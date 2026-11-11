package GoLogAPI.service;

import GoLogAPI.dto.occurrence.*;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantContext;
import GoLogAPI.mapper.OccurrenceMapper;
import GoLogAPI.model.*;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.repository.ShipmentRepository;
import GoLogAPI.repository.OccurrenceRepository;
import GoLogAPI.repository.TransportRepository;
import GoLogAPI.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OccurrenceService {

    private final OccurrenceRepository occurrenceRepository;
    private final ShipmentRepository shipmentRepository;
    private final TransportRepository transportRepository;
    private final UserRepository userRepository;
    private final OccurrenceMapper occurrenceMapper;
    private final GoLogAPI.infra.tenant.TenantGuard tenantGuard;

    public OccurrenceService(
            OccurrenceRepository occurrenceRepository, OccurrenceMapper occurrenceMapper,
            ShipmentRepository shipmentRepository, UserRepository userRepository,
            TransportRepository transportRepository, GoLogAPI.infra.tenant.TenantGuard tenantGuard)
    {
        this.tenantGuard = tenantGuard;
        this.occurrenceRepository = occurrenceRepository;
        this.occurrenceMapper = occurrenceMapper;
        this.transportRepository = transportRepository;
        this.userRepository = userRepository;
        this.shipmentRepository = shipmentRepository;
    }

    @Transactional
    public OccurrenceCreateResponse save(OccurrenceCreateRequest occurrenceCreateRequest){

        Shipment shipment = findShipment(occurrenceCreateRequest.shipmentId());
        Transport transport = findTransport(occurrenceCreateRequest.transportId());
        User sender = findSender(occurrenceCreateRequest.senderId());

        Occurrence occurrence = occurrenceMapper.toEntity(occurrenceCreateRequest);

        String type = occurrenceCreateRequest.type().trim();
        if(type.equalsIgnoreCase("Inicio") || type.equalsIgnoreCase("Início"))
             shipment.setStatus(ShipmentStatus.INICIADO);
        if(type.equalsIgnoreCase("Fim"))
            shipment.setStatus(ShipmentStatus.FINALIZADO);
        shipmentRepository.save(shipment);

        occurrence.setShipment(shipment);
        occurrence.setTransport(transport);
        occurrence.setSender(sender);

        Company company = transport.getTransporter() != null
                ? transport.getTransporter()
                : (shipment.getCompany() != null ? shipment.getCompany() : sender.getCompany());
        occurrence.setCompany(company);

        occurrenceRepository.save(occurrence);

        return occurrenceMapper.toCreateResponse(occurrence);
    }

    public OccurrenceResponse get(UUID id){
        Occurrence occurrence = findVisible(id);

        return occurrenceMapper.toResponse(occurrence);
    }

    public List<OccurrenceResponseList> getAll(){
        List<Occurrence> occurrences = tenantGuard.isGlobalView()
                ? occurrenceRepository.findAll()
                : occurrenceRepository.findByCompanyId(tenantGuard.scope());
        return occurrenceMapper.toResponseList(occurrences);
    }

    @Transactional
    public void delete(UUID id){
        Occurrence occurrence = findVisible(id);
        occurrenceRepository.delete(occurrence);
    }

    @Transactional
    public OccurrenceCreateResponse update(UUID id, OccurrenceCreateRequest occurrenceCreateRequest){
        Occurrence occurrence = findVisible(id);
        occurrence.setType(occurrenceCreateRequest.type());
        occurrence.setDateTime(occurrenceCreateRequest.dateTime());
        occurrence.setDescription(occurrenceCreateRequest.description());
        occurrence.setShipment(findShipment(occurrenceCreateRequest.shipmentId()));
        occurrence.setTransport(findTransport(occurrenceCreateRequest.transportId()));
        occurrence.setSender(findSender(occurrenceCreateRequest.senderId()));

        occurrenceRepository.save(occurrence);

        return occurrenceMapper.toCreateResponse(occurrence);
    }

    @Transactional
    public OccurrenceCreateResponse updatePartial(UUID id, OccurrenceUpdateRequest occurrenceUpdateRequest){
        Occurrence occurrence = findVisible(id);

        if(occurrenceUpdateRequest.type() != null && !occurrenceUpdateRequest.type().isBlank())
            occurrence.setType(occurrenceUpdateRequest.type());

        if(occurrenceUpdateRequest.description() != null && !occurrenceUpdateRequest.description().isBlank())
            occurrence.setDescription(occurrenceUpdateRequest.description());

        if(occurrenceUpdateRequest.attachment() != null && !occurrenceUpdateRequest.attachment().isBlank())
            occurrence.setAttachment(occurrenceUpdateRequest.attachment());

        if(occurrenceUpdateRequest.shipmentId() != null){
            Shipment shipment = findShipment(occurrenceUpdateRequest.shipmentId());

            occurrence.setShipment(shipment);
        }

        if(occurrenceUpdateRequest.transportId() != null){
            Transport transport = findTransport(occurrenceUpdateRequest.transportId());

            occurrence.setTransport(transport);
        }

        if(occurrenceUpdateRequest.senderId() != null){
            User sender = findSender(occurrenceUpdateRequest.senderId());

            occurrence.setSender(sender);
        }

        occurrenceRepository.save(occurrence);

        return occurrenceMapper.toCreateResponse(occurrence);
    }


    private Occurrence findVisible(UUID id) {
        Occurrence occurrence = occurrenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ocorrência não encontrada: ", id));
        tenantGuard.checkAccess(occurrence.getCompany(), id);
        return occurrence;
    }

    /** Motoristas registram ocorrências nas próprias rotas (o app não envia X-Tenant-Id). */
    private boolean isDriver() {
        return "DRIVER".equals(GoLogAPI.infra.tenant.TenantContext.getCurrentRole());
    }

    private Shipment findShipment(UUID id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Remessa não encontrada: ", id));
        if (!isDriver()) tenantGuard.checkAccess(shipment.getCompany(), id);
        return shipment;
    }

    private Transport findTransport(UUID id) {
        Transport transport = transportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transporte não encontrado: ", id));
        if (isDriver()) {
            UUID me = GoLogAPI.infra.tenant.TenantContext.getCurrentUserId();
            boolean mine = transport.getDriver() != null && transport.getDriver().getUser() != null
                    && transport.getDriver().getUser().getId().equals(me);
            if (!mine) throw new ResourceNotFoundException("Transporte não encontrado: ", id);
        } else {
            tenantGuard.checkAccess(transport.getTransporter(), id);
        }
        return transport;
    }

    private User findSender(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário emissor não encontrado: ", id));
    }
}
