package GoLogAPI.service;

import GoLogAPI.dto.shipment.*;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.*;
import GoLogAPI.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final AddressRepository addressRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final ShipmentTypeRepository shipmentTypeRepository;
    private final TypeTransportRepository typeTransportRepository;
    private final TransportRepository transportRepository;
    private final RouteStopRepository routeStopRepository;

    public ShipmentService(
            ShipmentRepository shipmentRepository, AddressRepository addressRepository,
            CompanyRepository companyRepository, UserRepository userRepository,
            ShipmentTypeRepository shipmentTypeRepository, TypeTransportRepository typeTransportRepository,
            TransportRepository transportRepository, RouteStopRepository routeStopRepository)
    {
        this.shipmentRepository = shipmentRepository;
        this.addressRepository = addressRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.shipmentTypeRepository = shipmentTypeRepository;
        this.typeTransportRepository = typeTransportRepository;
        this.transportRepository = transportRepository;
        this.routeStopRepository = routeStopRepository;
    }

    @Transactional
    public ShipmentCreateResponse save(ShipmentCreateRequest shipmentCreateRequest) {

        User user = userRepository.findById(shipmentCreateRequest.userId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.userId()));

        ShipmentType shipmentType = shipmentTypeRepository.findById(shipmentCreateRequest.shipmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.shipmentTypeId()));

        TypeTransport typeTransport = typeTransportRepository.findById(shipmentCreateRequest.typeTransportId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.typeTransportId()));

        Address shipmentAddress = addressRepository.findById(shipmentCreateRequest.addressId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.addressId()));

        Company customer = companyRepository.findById(shipmentCreateRequest.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.customerId()));

        Shipment.ShipmentBuilder shipmentBuilder = Shipment.builder()
                .weight(shipmentCreateRequest.weight())
                .typeOperation(shipmentCreateRequest.typeOperation())
                .volume(shipmentCreateRequest.volume())
                .schedulind(shipmentCreateRequest.schedulind())
                .user(user)
                .status("")
                .shipmentType(shipmentType)
                .address(shipmentAddress)
                .typeTransport(typeTransport)
                .customer(customer);

        if(shipmentCreateRequest.operationOrigemId() != null) {
            Shipment operationOrigem = shipmentRepository.findById(shipmentCreateRequest.operationOrigemId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.operationOrigemId()));
            shipmentBuilder.operationOrigem(operationOrigem);
        }

        Shipment shipment = shipmentBuilder.build();

        shipmentRepository.save(shipment);

        if(shipment.getOperationOrigem() != null){
            UUID collectId = shipment.getOperationOrigem().getId();

            ShipmentRepository.TotalTotals totals = shipmentRepository.findTotalsByOrigemId(collectId);

            Shipment collect = shipmentRepository.findById(collectId)
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, collectId));

            collect.setWeight(totals.getTotalWeight() != null ? totals.getTotalWeight() : 0.0);
            collect.setVolume(totals.getTotalVolume() != null ? totals.getTotalVolume() : 0.0);

            shipmentRepository.save(collect);
        }

        return new ShipmentCreateResponse(
                shipment.getId(),
                shipment.getTypeOperation(),
                shipment.getWeight(),
                shipment.getVolume(),
                shipment.getSchedulind(),
                shipment.getStatus(),
                shipment.getUser().getId(),
                shipment.getShipmentType().getId(),
                shipment.getTypeTransport().getId(),
                shipment.getAddress().getId(),
                shipment.getCustomer().getId(),
                shipment.getOperationOrigem() != null ? shipment.getOperationOrigem().getId() : null
        );
    }

    public ShipmentResponse get(UUID id){
        Shipment shipment = shipmentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE,id));

        RouteStop routeStop = routeStopRepository.findById(shipment.getId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        return new ShipmentResponse(
                shipment.getId(),
                shipment.getTypeOperation(),
                shipment.getWeight(),
                shipment.getVolume(),
                shipment.getSchedulind(),
                shipment.getStatus(),
                shipment.getUser(),
                shipment.getShipmentType(),
                shipment.getTypeTransport(),
                shipment.getAddress(),
                shipment.getCustomer(),
                shipment.getOperationOrigem() != null ? shipment.getOperationOrigem() : null
        );
    }

    public List<ShipmentResponseList> getAll(){
        List<Shipment> shipments = shipmentRepository.findAll();
        return shipments.stream()
                .map(shipment -> new ShipmentResponseList(
                        shipment.getId(),
                        shipment.getTypeOperation(),
                        shipment.getWeight(),
                        shipment.getVolume(),
                        shipment.getSchedulind(),
                        shipment.getStatus(),
                        shipment.getUser(),
                        shipment.getShipmentType(),
                        shipment.getTypeTransport(),
                        shipment.getAddress(),
                        shipment.getCustomer(),
                        shipment.getOperationOrigem()
                ))
                .toList();
    }

    public List<ShipmentResponseListPersonalized> getAllWithQuery() {
        List<Object[]> results = shipmentRepository.findAllShipmentsWithRoutesAndTransportMandatory();

        return results.stream()
                .map(result -> {
                    Shipment shipment = (Shipment) result[0];
                    RouteStop routeStop = (RouteStop) result[1];
                    Transport transport = (Transport) result[2];

                    return new ShipmentResponseListPersonalized(
                            shipment.getId(),
                            shipment.getTypeOperation(),
                            shipment.getWeight(),
                            shipment.getVolume(),
                            shipment.getSchedulind(),
                            shipment.getStatus(),
                            shipment.getShipmentType(),
                            shipment.getTypeTransport(),
                            shipment.getCustomer(),
                            shipment.getOperationOrigem(),
                            transport,
                            routeStop
                    );
                })
                .toList();
    }

    @Transactional
    public void delete(UUID id){
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        shipmentRepository.delete(shipment);
    }

    @Transactional
    public ShipmentUpdateResponse update(UUID id, ShipmentCreateRequest shipmentCreateRequest){

        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        User user = userRepository.findById(shipmentCreateRequest.userId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.userId()));

        ShipmentType shipmentType = shipmentTypeRepository.findById(shipmentCreateRequest.shipmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.shipmentTypeId()));

        TypeTransport typeTransport = typeTransportRepository.findById(shipmentCreateRequest.typeTransportId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.typeTransportId()));

        Address deliveryAddress = addressRepository.findById(shipmentCreateRequest.addressId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.addressId()));

        Company customerDelivery = companyRepository.findById(shipmentCreateRequest.customerId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.customerId()));

        if(shipmentCreateRequest.operationOrigemId() != null) {
            Shipment operationOrigem = shipmentRepository.findById(shipmentCreateRequest.operationOrigemId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.operationOrigemId()));
            shipment.setOperationOrigem(operationOrigem);
        }

        shipment.setTypeOperation(shipmentCreateRequest.typeOperation());
        shipment.setWeight(shipmentCreateRequest.weight());
        shipment.setVolume(shipmentCreateRequest.volume());
        shipment.setSchedulind(shipmentCreateRequest.schedulind());
        shipment.setStatus(shipmentCreateRequest.status());
        shipment.setUser(user);
        shipment.setShipmentType(shipmentType);
        shipment.setTypeTransport(typeTransport);
        shipment.setAddress(deliveryAddress);
        shipment.setCustomer(customerDelivery);

        shipmentRepository.save(shipment);

        if(shipment.getOperationOrigem() != null){
            UUID collectId = shipment.getOperationOrigem().getId();

            ShipmentRepository.TotalTotals totals = shipmentRepository.findTotalsByOrigemId(collectId);

            Shipment collect = shipmentRepository.findById(collectId)
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, collectId));

            collect.setWeight(totals.getTotalWeight() != null ? totals.getTotalWeight() : 0.0);
            collect.setVolume(totals.getTotalVolume() != null ? totals.getTotalVolume() : 0.0);

            shipmentRepository.save(collect);
        }

        return new ShipmentUpdateResponse(
                shipment.getId(),
                shipment.getTypeOperation(),
                shipment.getWeight(),
                shipment.getVolume(),
                shipment.getSchedulind(),
                shipment.getStatus(),
                shipment.getUser().getId(),
                shipment.getShipmentType().getId(),
                shipment.getTypeTransport().getId(),
                shipment.getAddress().getId(),
                shipment.getCustomer().getId(),
                shipment.getOperationOrigem() != null ? shipment.getOperationOrigem().getId() : null
        );
    }

    @Transactional
    public ShipmentCreateResponse updatePartial(UUID id, ShipmentUpdateRequest shipmentUpdateRequest){

        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        if(shipmentUpdateRequest.weight() != null)
            shipment.setWeight(shipmentUpdateRequest.weight());

        if(shipmentUpdateRequest.volume() != null)
            shipment.setVolume(shipmentUpdateRequest.volume());

        if(shipmentUpdateRequest.schedulind() != null)
            shipment.setSchedulind(shipmentUpdateRequest.schedulind());

        if(shipmentUpdateRequest.status() != null && !shipmentUpdateRequest.status().isBlank())
            shipment.setStatus(shipmentUpdateRequest.status());

        if(shipmentUpdateRequest.userId() != null) {
            User user = userRepository.findById(shipmentUpdateRequest.userId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentUpdateRequest.userId()));
            shipment.setUser(user);
        }

        if(shipmentUpdateRequest.shipmentTypeId() != null){
            ShipmentType shipmentType = shipmentTypeRepository.findById(shipmentUpdateRequest.shipmentTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentUpdateRequest.shipmentTypeId()));
            shipment.setShipmentType(shipmentType);
        }


        if(shipmentUpdateRequest.typeTransportId() != null){
            TypeTransport typeTransport = typeTransportRepository.findById(shipmentUpdateRequest.typeTransportId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentUpdateRequest.typeTransportId()));
            shipment.setTypeTransport(typeTransport);
        }

        if(shipmentUpdateRequest.addressId() != null){
            Address addressDestination = addressRepository.findById(shipmentUpdateRequest.addressId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentUpdateRequest.addressId()));
            shipment.setAddress(addressDestination);
        }

        if(shipmentUpdateRequest.customerId() != null){
            Company customerDelivery = companyRepository.findById(shipmentUpdateRequest.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentUpdateRequest.customerId()));
            shipment.setCustomer(customerDelivery);
        }

        if(shipmentUpdateRequest.operationOrigemId() != null) {
            Shipment operationOrigem = shipmentRepository.findById(shipmentUpdateRequest.operationOrigemId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentUpdateRequest.operationOrigemId()));
            shipment.setOperationOrigem(operationOrigem);
        }

        shipmentRepository.save(shipment);

        if(shipment.getOperationOrigem() != null){
            UUID collectId = shipment.getOperationOrigem().getId();

            ShipmentRepository.TotalTotals totals = shipmentRepository.findTotalsByOrigemId(collectId);

            Shipment collect = shipmentRepository.findById(collectId)
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, collectId));

            collect.setWeight(totals.getTotalWeight() != null ? totals.getTotalWeight() : 0.0);
            collect.setVolume(totals.getTotalVolume() != null ? totals.getTotalVolume() : 0.0);

            shipmentRepository.save(collect);
        }

        return new ShipmentCreateResponse(
                shipment.getId(),
                shipment.getTypeOperation(),
                shipment.getWeight(),
                shipment.getVolume(),
                shipment.getSchedulind(),
                shipment.getStatus(),
                shipment.getUser().getId(),
                shipment.getShipmentType().getId(),
                shipment.getTypeTransport().getId(),
                shipment.getAddress().getId(),
                shipment.getCustomer().getId(),
                shipment.getOperationOrigem() != null ? shipment.getOperationOrigem().getId() : null
        );
    }
}
