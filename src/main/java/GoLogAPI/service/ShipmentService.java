package GoLogAPI.service;

import GoLogAPI.dto.shipment.*;
import GoLogAPI.exception.BusinessException;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantContext;
import GoLogAPI.infra.tenant.TenantGuard;
import GoLogAPI.model.*;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.model.enums.TypeOperation;
import GoLogAPI.repository.*;
import GoLogAPI.validation.ShipementValidate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Remessas (coletas e entregas). Toda remessa pertence ao tenant que a cadastrou; cliente, endereço,
 * responsável e tipos referenciados precisam estar visíveis nesse mesmo tenant.
 *
 * <p>Uma ENTREGA pode estar vinculada a uma COLETA de origem ({@code operationOrigem}); nesse caso o peso e
 * o volume da coleta são recalculados como a soma das entregas filhas.</p>
 */
@Service
@Transactional(readOnly = true)
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final ShipmentTypeRepository shipmentTypeRepository;
    private final TypeTransportRepository typeTransportRepository;
    private final ShipementValidate shipementValidate;
    private final CompanyService companyService;
    private final TenantGuard tenantGuard;

    public ShipmentService(
            ShipmentRepository shipmentRepository, AddressRepository addressRepository,
            UserRepository userRepository, ShipmentTypeRepository shipmentTypeRepository,
            TypeTransportRepository typeTransportRepository, ShipementValidate shipementValidate,
            CompanyService companyService, TenantGuard tenantGuard)
    {
        this.shipmentRepository = shipmentRepository;
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.shipmentTypeRepository = shipmentTypeRepository;
        this.typeTransportRepository = typeTransportRepository;
        this.shipementValidate = shipementValidate;
        this.companyService = companyService;
        this.tenantGuard = tenantGuard;
    }

    @Transactional
    public ShipmentCreateResponse save(ShipmentCreateRequest request) {
        assertNotInPast(request.schedulind());
        shipementValidate.validate(request);

        Company owner = tenantGuard.ownerForWrite(null);
        Shipment shipment = Shipment.builder()
                .weight(request.weight())
                .typeOperation(request.typeOperation())
                .volume(request.volume())
                .schedulind(request.schedulind())
                .user(findUser(request.userId()))
                .status(ShipmentStatus.PENDENTE)
                .shipmentType(findShipmentType(request.shipmentTypeId()))
                .address(findAddress(request.addressId()))
                .typeTransport(findTypeTransport(request.typeTransportId()))
                .customer(companyService.findVisible(request.customerId()))
                .company(owner)
                .build();
        shipment.setOperationOrigem(resolveOrigin(request.typeOperation(), request.operationOrigemId(), null));

        shipmentRepository.save(shipment);
        recalculateCollectTotals(shipment.getOperationOrigem());
        return toCreateResponse(shipment);
    }

    public ShipmentResponse get(UUID id){
        Shipment s = findVisible(id);
        return new ShipmentResponse(
                s.getId(), s.getTypeOperation(), s.getWeight(), s.getVolume(), s.getSchedulind(), s.getStatus(),
                s.getUser(), s.getShipmentType(), s.getTypeTransport(), s.getAddress(), s.getCustomer(),
                s.getOperationOrigem());
    }

    public List<ShipmentResponseList> getAll(){
        return visibleShipments().stream().map(ShipmentService::toListItem).toList();
    }

    public List<ShipmentResponseList> getByStatus(ShipmentStatus status){
        return visibleShipments().stream()
                .filter(s -> s.getStatus() == status)
                .map(ShipmentService::toListItem)
                .toList();
    }

    /**
     * Visão consolidada remessa + parada + transporte usada pelo painel de monitoramento e pelo app.
     * Motoristas enxergam apenas as próprias rotas, independentemente do tenant.
     */
    public List<ShipmentResponseListPersonalized> getAllWithQuery(UUID userId) {
        boolean isDriver = "DRIVER".equals(TenantContext.getCurrentRole());
        UUID effectiveUser = isDriver ? TenantContext.getCurrentUserId() : userId;
        UUID companyScope = (isDriver || tenantGuard.isGlobalView()) ? null : tenantGuard.scope();

        List<Object[]> results = shipmentRepository.findAllShipmentsWithRoutesAndTransportMandatoryByCompanyId(companyScope, effectiveUser);
        return results.stream()
                .map(result -> {
                    Shipment shipment = (Shipment) result[0];
                    RouteStop routeStop = (RouteStop) result[1];
                    Transport transport = (Transport) result[2];
                    return new ShipmentResponseListPersonalized(
                            shipment.getId(), shipment.getTypeOperation(), shipment.getWeight(), shipment.getVolume(),
                            shipment.getSchedulind(), shipment.getStatus(), shipment.getShipmentType(),
                            shipment.getTypeTransport(), shipment.getAddress(), shipment.getCustomer(),
                            shipment.getOperationOrigem(), transport, routeStop);
                })
                .toList();
    }

    @Transactional
    public void delete(UUID id){
        Shipment shipment = findVisible(id);
        if (shipment.getStatus() == ShipmentStatus.INICIADO) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Não é possível excluir uma remessa em andamento.");
        }
        if (shipment.getTypeOperation() == TypeOperation.COLETA && !shipmentRepository.findByOperationOrigem(shipment).isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Esta coleta possui entregas vinculadas. Exclua ou desvincule as entregas antes.");
        }
        Shipment origin = shipment.getOperationOrigem();
        shipmentRepository.delete(shipment);
        shipmentRepository.flush();
        recalculateCollectTotals(origin);
    }

    @Transactional
    public ShipmentUpdateResponse update(UUID id, ShipmentCreateRequest request){
        Shipment shipment = findVisible(id);
        if (!Objects.equals(shipment.getSchedulind(), request.schedulind())) {
            assertNotInPast(request.schedulind());
        }
        shipementValidate.validate(request);

        Shipment previousOrigin = shipment.getOperationOrigem();
        shipment.setTypeOperation(request.typeOperation());
        shipment.setWeight(request.weight());
        shipment.setVolume(request.volume());
        shipment.setSchedulind(request.schedulind());
        if (request.status() != null) shipment.setStatus(request.status());
        shipment.setUser(findUser(request.userId()));
        shipment.setShipmentType(findShipmentType(request.shipmentTypeId()));
        shipment.setTypeTransport(findTypeTransport(request.typeTransportId()));
        shipment.setAddress(findAddress(request.addressId()));
        shipment.setCustomer(companyService.findVisible(request.customerId()));
        shipment.setOperationOrigem(resolveOrigin(request.typeOperation(), request.operationOrigemId(), id));

        shipmentRepository.save(shipment);
        recalculateCollectTotals(shipment.getOperationOrigem());
        if (previousOrigin != null && !Objects.equals(previousOrigin, shipment.getOperationOrigem())) {
            recalculateCollectTotals(previousOrigin);
        }
        // Coleta com entregas filhas: peso/volume são sempre a soma das filhas (o formulário envia 0)
        if (shipment.getTypeOperation() == TypeOperation.COLETA && !shipmentRepository.findByOperationOrigem(shipment).isEmpty()) {
            shipmentRepository.flush();
            recalculateCollectTotals(shipment);
            shipment = shipmentRepository.findById(id).orElse(shipment);
        }

        return new ShipmentUpdateResponse(
                shipment.getId(), shipment.getTypeOperation(), shipment.getWeight(), shipment.getVolume(),
                shipment.getSchedulind(), shipment.getStatus(), shipment.getUser().getId(),
                shipment.getShipmentType().getId(), shipment.getTypeTransport().getId(), shipment.getAddress().getId(),
                shipment.getCustomer().getId(),
                shipment.getOperationOrigem() != null ? shipment.getOperationOrigem().getId() : null);
    }

    @Transactional
    public ShipmentCreateResponse updatePartial(UUID id, ShipmentUpdateRequest request){
        Shipment shipment = findVisible(id);
        shipementValidate.validate(request);
        Shipment previousOrigin = shipment.getOperationOrigem();

        if(request.typeOperation() != null) shipment.setTypeOperation(request.typeOperation());
        if(request.weight() != null) shipment.setWeight(request.weight());
        if(request.volume() != null) shipment.setVolume(request.volume());
        if(request.schedulind() != null) shipment.setSchedulind(request.schedulind());
        if(request.status() != null) shipment.setStatus(request.status());
        if(request.userId() != null) shipment.setUser(findUser(request.userId()));
        if(request.shipmentTypeId() != null) shipment.setShipmentType(findShipmentType(request.shipmentTypeId()));
        if(request.typeTransportId() != null) shipment.setTypeTransport(findTypeTransport(request.typeTransportId()));
        if(request.addressId() != null) shipment.setAddress(findAddress(request.addressId()));
        if(request.customerId() != null) shipment.setCustomer(companyService.findVisible(request.customerId()));
        if(request.operationOrigemId() != null)
            shipment.setOperationOrigem(resolveOrigin(shipment.getTypeOperation(), request.operationOrigemId(), id));

        shipmentRepository.save(shipment);
        recalculateCollectTotals(shipment.getOperationOrigem());
        if (previousOrigin != null && !Objects.equals(previousOrigin, shipment.getOperationOrigem())) {
            recalculateCollectTotals(previousOrigin);
        }
        return toCreateResponse(shipment);
    }

    public Shipment findVisible(UUID id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remessa não encontrada: ", id));
        tenantGuard.checkAccess(shipment.getCompany(), id);
        return shipment;
    }

    private List<Shipment> visibleShipments() {
        return tenantGuard.isGlobalView()
                ? shipmentRepository.findAll()
                : shipmentRepository.findByCompanyId(tenantGuard.scope());
    }

    /** Entregas só podem ter uma COLETA do mesmo tenant como origem; coletas não têm origem. */
    private Shipment resolveOrigin(TypeOperation type, UUID originId, UUID selfId) {
        if (originId == null) return null;
        if (type == TypeOperation.COLETA) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Uma coleta não pode ter coleta de origem. Use o tipo ENTREGA.");
        }
        if (originId.equals(selfId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Uma remessa não pode ser origem de si mesma.");
        }
        Shipment origin = findVisible(originId);
        if (origin.getTypeOperation() != TypeOperation.COLETA) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "A remessa de origem precisa ser do tipo COLETA.");
        }
        return origin;
    }

    private void recalculateCollectTotals(Shipment collect) {
        if (collect == null) return;
        ShipmentRepository.TotalTotals totals = shipmentRepository.findTotalsByOrigemId(collect.getId());
        Shipment fresh = shipmentRepository.findById(collect.getId()).orElse(null);
        if (fresh == null) return;
        fresh.setWeight(totals != null && totals.getTotalWeight() != null ? totals.getTotalWeight() : 0.0);
        fresh.setVolume(totals != null && totals.getTotalVolume() != null ? totals.getTotalVolume() : 0.0);
        shipmentRepository.save(fresh);
    }

    private static void assertNotInPast(LocalDateTime when) {
        if (when != null && when.isBefore(LocalDateTime.now().minusMinutes(5))) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "A data de agendamento da remessa deve ser no presente ou no futuro.");
        }
    }

    private User findUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário responsável não encontrado: ", id));
        tenantGuard.checkAccess(user.getCompany(), id);
        return user;
    }

    private ShipmentType findShipmentType(UUID id) {
        ShipmentType type = shipmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de carga não encontrado: ", id));
        tenantGuard.checkCatalogRead(type.getCompany(), id);
        return type;
    }

    private TypeTransport findTypeTransport(UUID id) {
        TypeTransport type = typeTransportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de transporte não encontrado: ", id));
        tenantGuard.checkCatalogRead(type.getCompany(), id);
        return type;
    }

    private Address findAddress(UUID id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado: ", id));
        tenantGuard.checkAccess(address.getCompany(), id);
        return address;
    }

    private static ShipmentResponseList toListItem(Shipment s) {
        return new ShipmentResponseList(
                s.getId(), s.getTypeOperation(), s.getWeight(), s.getVolume(), s.getSchedulind(), s.getStatus(),
                s.getUser(), s.getShipmentType(), s.getTypeTransport(), s.getAddress(), s.getCustomer(),
                s.getOperationOrigem());
    }

    private static ShipmentCreateResponse toCreateResponse(Shipment s) {
        return new ShipmentCreateResponse(
                s.getId(), s.getTypeOperation(), s.getWeight(), s.getVolume(), s.getSchedulind(), s.getStatus(),
                s.getUser().getId(), s.getShipmentType().getId(), s.getTypeTransport().getId(), s.getAddress().getId(),
                s.getCustomer().getId(), s.getOperationOrigem() != null ? s.getOperationOrigem().getId() : null);
    }
}
