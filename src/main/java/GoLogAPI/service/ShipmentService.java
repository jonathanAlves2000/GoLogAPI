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

    public ShipmentService(
            ShipmentRepository shipmentRepository, AddressRepository addressRepository,
            CompanyRepository companyRepository, UserRepository userRepository,
            ShipmentTypeRepository shipmentTypeRepository, TypeTransportRepository typeTransportRepository,
            TransportRepository transportRepository)
    {
        this.shipmentRepository = shipmentRepository;
        this.addressRepository = addressRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.shipmentTypeRepository = shipmentTypeRepository;
        this.typeTransportRepository = typeTransportRepository;
        this.transportRepository = transportRepository;
    }

    @Transactional
    public ShipmentCreateResponse save(ShipmentCreateRequest shipmentCreateRequest) {

        User user = userRepository.findById(shipmentCreateRequest.userId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.userId()));

        ShipmentType shipmentType = shipmentTypeRepository.findById(shipmentCreateRequest.shipmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.shipmentTypeId()));

        Transport transport = transportRepository.findById(shipmentCreateRequest.transportId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.transportId()));

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
                .status("AGUARDANDO INICIO")
                .shippingSequence(shipmentCreateRequest.shippingSequence())
                .user(user)
                .shipmentType(shipmentType)
                .transport(transport)
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
                shipment.getShippingSequence(),
                shipment.getUser().getId(),
                shipment.getShipmentType().getId(),
                shipment.getTransport().getId(),
                shipment.getTypeTransport().getId(),
                shipment.getAddress().getId(),
                shipment.getCustomer().getId(),
                shipment.getOperationOrigem().getId() != null ? shipment.getOperationOrigem().getId() : null
        );
    }

    public ShipmentResponse get(UUID id){
        Shipment shipment = shipmentRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE,id));

        return new ShipmentResponse(
                shipment.getId(),
                shipment.getTypeOperation(),
                shipment.getWeight(),
                shipment.getVolume(),
                shipment.getSchedulind(),
                shipment.getRoutePlanned(),
                shipment.getRouteCompleted(),
                shipment.getCalculatedDistance(),
                shipment.getCalculatedDuration(),
                shipment.getStatus(),
                shipment.getCalculatedWait(),
                shipment.getShippingSequence(),
                shipment.getCalculatedCost(),
                shipment.getUser(),
                shipment.getShipmentType(),
                shipment.getTransport(),
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
                        shipment.getRoutePlanned(),
                        shipment.getRouteCompleted(),
                        shipment.getCalculatedDistance(),
                        shipment.getCalculatedDuration(),
                        shipment.getStatus(),
                        shipment.getShippingSequence(),
                        shipment.getCalculatedWait(),
                        shipment.getCalculatedCost(),
                        shipment.getUser(),
                        shipment.getShipmentType(),
                        shipment.getTransport(),
                        shipment.getTypeTransport(),
                        shipment.getAddress(),
                        shipment.getCustomer(),
                        shipment.getOperationOrigem() != null ? shipment.getOperationOrigem() : null
                ))
                .toList();
    }

    @Transactional
    public void delete(UUID id){
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        shipmentRepository.delete(shipment);
    }

    @Transactional
    public ShipmentCreateResponse update(UUID id, ShipmentCreateRequest shipmentCreateRequest){

        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        User user = userRepository.findById(shipmentCreateRequest.userId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.userId()));

        ShipmentType shipmentType = shipmentTypeRepository.findById(shipmentCreateRequest.shipmentTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.shipmentTypeId()));

        Transport transport = transportRepository.findById(shipmentCreateRequest.transportId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentCreateRequest.transportId()));

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
        shipment.setShippingSequence(shipmentCreateRequest.shippingSequence());
        shipment.setUser(user);
        shipment.setShipmentType(shipmentType);
        shipment.setTransport(transport);
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

        return new ShipmentCreateResponse(
                shipment.getId(),
                shipment.getTypeOperation(),
                shipment.getWeight(),
                shipment.getVolume(),
                shipment.getSchedulind(),
                shipment.getStatus(),
                shipment.getShippingSequence(),
                shipment.getUser().getId(),
                shipment.getShipmentType().getId(),
                shipment.getTransport().getId(),
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

        if(shipmentUpdateRequest.shippingSequence() != null)
            shipment.setShippingSequence(shipmentUpdateRequest.shippingSequence());

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

        if(shipmentUpdateRequest.transportId() != null){
            Transport transport = transportRepository.findById(shipmentUpdateRequest.transportId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentUpdateRequest.transportId()));
            shipment.setTransport(transport);
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
                shipment.getShippingSequence(),
                shipment.getUser().getId(),
                shipment.getShipmentType().getId(),
                shipment.getTransport().getId(),
                shipment.getTypeTransport().getId(),
                shipment.getAddress().getId(),
                shipment.getCustomer().getId(),
                shipment.getOperationOrigem() != null ? shipment.getOperationOrigem().getId() : null
        );
    }
}
