package GoLogAPI.service;

import GoLogAPI.dto.delivery.*;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.*;
import GoLogAPI.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final AddressRepository addressRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final DeliveryTypeRepository deliveryTypeRepository;
    private final TypeTransportRepository typeTransportRepository;
    private final TransportRepository transportRepository;

    public DeliveryService(
            DeliveryRepository deliveryRepository, AddressRepository addressRepository,
            CompanyRepository companyRepository, UserRepository userRepository,
            DeliveryTypeRepository deliveryTypeRepository, TypeTransportRepository typeTransportRepository,
            TransportRepository transportRepository)
    {
        this.deliveryRepository = deliveryRepository;
        this.addressRepository = addressRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.deliveryTypeRepository = deliveryTypeRepository;
        this.typeTransportRepository = typeTransportRepository;
        this.transportRepository = transportRepository;
    }

    @Transactional
    public DeliveryCreateResponse save(DeliveryCreateRequest deliveryCreateRequest) {

        User user = userRepository.findById(deliveryCreateRequest.userId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.userId()));

        DeliveryType deliveryType = deliveryTypeRepository.findById(deliveryCreateRequest.deliveryTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.deliveryTypeId()));

        Transport transport = transportRepository.findById(deliveryCreateRequest.transportId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.transportId()));

        TypeTransport typeTransport = typeTransportRepository.findById(deliveryCreateRequest.typeTransportId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.typeTransportId()));

        Address addressOrigin = addressRepository.findById(deliveryCreateRequest.originAdrressId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.originAdrressId()));

        Address addressDestination = addressRepository.findById(deliveryCreateRequest.destinationAddressId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.destinationAddressId()));

        Company customerCollects = companyRepository.findById(deliveryCreateRequest.customerCollectsId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.customerCollectsId()));

        Company customerDelivery = companyRepository.findById(deliveryCreateRequest.customerDeliveryId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.customerDeliveryId()));

        Delivery delivery = Delivery.builder()
                .weight(deliveryCreateRequest.weight())
                .volume(deliveryCreateRequest.volume())
                .scheduledCollection(deliveryCreateRequest.scheduledCollection())
                .scheduledDelivery(deliveryCreateRequest.scheduledDelivery())
                .routePlanned(deliveryCreateRequest.routePlanned())
                .routeCompleted(deliveryCreateRequest.routeCompleted())
                .status(deliveryCreateRequest.status())
                .deliverySequence(deliveryCreateRequest.deliverySequence())
                .user(user)
                .deliveryType(deliveryType)
                .transport(transport)
                .typeTransport(typeTransport)
                .originAdrress(addressOrigin)
                .destinationAddress(addressDestination)
                .customerCollects(customerCollects)
                .customerDelivery(customerDelivery)
                .build();

        deliveryRepository.save(delivery);

        return new DeliveryCreateResponse(
                delivery.getId(),
                delivery.getWeight(),
                delivery.getVolume(),
                delivery.getScheduledCollection(),
                delivery.getScheduledDelivery(),
                delivery.getRoutePlanned(),
                delivery.getRouteCompleted(),
                delivery.getStatus(),
                delivery.getDeliverySequence(),
                delivery.getUser().getId(),
                delivery.getDeliveryType().getId(),
                delivery.getTransport().getId(),
                delivery.getTypeTransport().getId(),
                delivery.getOriginAdrress().getId(),
                delivery.getDestinationAddress().getId(),
                delivery.getCustomerCollects().getId(),
                delivery.getCustomerDelivery().getId()
        );
    }

    public DeliveryResponse get(UUID id){
        Delivery delivery = deliveryRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE,id));

        return new DeliveryResponse(
                delivery.getId(),
                delivery.getWeight(),
                delivery.getVolume(),
                delivery.getScheduledCollection(),
                delivery.getScheduledDelivery(),
                delivery.getRoutePlanned(),
                delivery.getRouteCompleted(),
                delivery.getStatus(),
                delivery.getDeliverySequence(),
                delivery.getUser(),
                delivery.getDeliveryType(),
                delivery.getTransport(),
                delivery.getTypeTransport(),
                delivery.getOriginAdrress(),
                delivery.getDestinationAddress(),
                delivery.getCustomerCollects(),
                delivery.getCustomerDelivery()
        );
    }

    public List<DeliveryResponseList> getAll(){
        List<Delivery> deliveries = deliveryRepository.findAll();
        return deliveries.stream()
                .map(delivery -> new DeliveryResponseList(
                        delivery.getId(),
                        delivery.getWeight(),
                        delivery.getVolume(),
                        delivery.getScheduledCollection(),
                        delivery.getScheduledDelivery(),
                        delivery.getRoutePlanned(),
                        delivery.getRouteCompleted(),
                        delivery.getStatus(),
                        delivery.getDeliverySequence(),
                        delivery.getUser().getId(),
                        delivery.getDeliveryType().getId(),
                        delivery.getTransport().getId(),
                        delivery.getTypeTransport().getId(),
                        delivery.getOriginAdrress().getId(),
                        delivery.getDestinationAddress().getId(),
                        delivery.getCustomerCollects().getId(),
                        delivery.getCustomerDelivery().getId()
                ))
                .toList();
    }

    @Transactional
    public void delete(UUID id){
        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        deliveryRepository.delete(delivery);
    }

    @Transactional
    public DeliveryCreateResponse update(UUID id, DeliveryCreateRequest deliveryCreateRequest){

        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        User user = userRepository.findById(deliveryCreateRequest.userId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.userId()));

        DeliveryType deliveryType = deliveryTypeRepository.findById(deliveryCreateRequest.deliveryTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.deliveryTypeId()));

        Transport transport = transportRepository.findById(deliveryCreateRequest.transportId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.transportId()));

        TypeTransport typeTransport = typeTransportRepository.findById(deliveryCreateRequest.typeTransportId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.typeTransportId()));

        Address addressOrigin = addressRepository.findById(deliveryCreateRequest.originAdrressId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.originAdrressId()));

        Address addressDestination = addressRepository.findById(deliveryCreateRequest.destinationAddressId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.destinationAddressId()));

        Company customerCollects = companyRepository.findById(deliveryCreateRequest.customerCollectsId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.customerCollectsId()));

        Company customerDelivery = companyRepository.findById(deliveryCreateRequest.customerDeliveryId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.customerDeliveryId()));

        delivery.setWeight(deliveryCreateRequest.weight());
        delivery.setVolume(deliveryCreateRequest.volume());
        delivery.setScheduledCollection(deliveryCreateRequest.scheduledCollection());
        delivery.setScheduledDelivery(deliveryCreateRequest.scheduledDelivery());
        delivery.setRoutePlanned(deliveryCreateRequest.routePlanned());
        delivery.setRouteCompleted(deliveryCreateRequest.routeCompleted());
        delivery.setStatus(deliveryCreateRequest.status());
        delivery.setDeliverySequence(deliveryCreateRequest.deliverySequence());
        delivery.setUser(user);
        delivery.setDeliveryType(deliveryType);
        delivery.setTransport(transport);
        delivery.setTypeTransport(typeTransport);
        delivery.setOriginAdrress(addressOrigin);
        delivery.setDestinationAddress(addressDestination);
        delivery.setCustomerCollects(customerCollects);
        delivery.setCustomerDelivery(customerDelivery);

        deliveryRepository.save(delivery);

        return new DeliveryCreateResponse(
                delivery.getId(),
                delivery.getWeight(),
                delivery.getVolume(),
                delivery.getScheduledCollection(),
                delivery.getScheduledDelivery(),
                delivery.getRoutePlanned(),
                delivery.getRouteCompleted(),
                delivery.getStatus(),
                delivery.getDeliverySequence(),
                delivery.getUser().getId(),
                delivery.getDeliveryType().getId(),
                delivery.getTransport().getId(),
                delivery.getTypeTransport().getId(),
                delivery.getOriginAdrress().getId(),
                delivery.getDestinationAddress().getId(),
                delivery.getCustomerCollects().getId(),
                delivery.getCustomerDelivery().getId()
        );
    }

    @Transactional
    public DeliveryCreateResponse updatePartial(UUID id, DeliveryUpdateRequest deliveryUpdateRequest){

        Delivery delivery = deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        if(deliveryUpdateRequest.weight() != null)
            delivery.setWeight(deliveryUpdateRequest.weight());

        if(deliveryUpdateRequest.volume() != null)
            delivery.setVolume(deliveryUpdateRequest.volume());

        if(deliveryUpdateRequest.scheduledCollection() != null)
            delivery.setScheduledCollection(deliveryUpdateRequest.scheduledCollection());

        if(deliveryUpdateRequest.scheduledDelivery() != null)
            delivery.setScheduledDelivery(deliveryUpdateRequest.scheduledDelivery());

        if(deliveryUpdateRequest.routePlanned() != null && !deliveryUpdateRequest.routePlanned().isBlank())
            delivery.setRoutePlanned(deliveryUpdateRequest.routePlanned());

        if(deliveryUpdateRequest.routeCompleted() != null && !deliveryUpdateRequest.routeCompleted().isBlank())
            delivery.setRouteCompleted(deliveryUpdateRequest.routeCompleted());

        if(deliveryUpdateRequest.status() != null && !deliveryUpdateRequest.status().isBlank())
            delivery.setStatus(deliveryUpdateRequest.status());

        if(deliveryUpdateRequest.deliverySequence() != null)
            delivery.setDeliverySequence(deliveryUpdateRequest.deliverySequence());

        if(deliveryUpdateRequest.userId() != null) {
            User user = userRepository.findById(deliveryUpdateRequest.userId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryUpdateRequest.userId()));
            delivery.setUser(user);
        }

        if(deliveryUpdateRequest.deliveryTypeId() != null){
            DeliveryType deliveryType = deliveryTypeRepository.findById(deliveryUpdateRequest.deliveryTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryUpdateRequest.deliveryTypeId()));
            delivery.setDeliveryType(deliveryType);
        }

        if(deliveryUpdateRequest.transportId() != null){
            Transport transport = transportRepository.findById(deliveryUpdateRequest.transportId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryUpdateRequest.transportId()));
            delivery.setTransport(transport);
        }

        if(deliveryUpdateRequest.typeTransportId() != null){
            TypeTransport typeTransport = typeTransportRepository.findById(deliveryUpdateRequest.typeTransportId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryUpdateRequest.typeTransportId()));
            delivery.setTypeTransport(typeTransport);
        }

        if(deliveryUpdateRequest.originAdrressId() != null){
            Address addressOrigin = addressRepository.findById(deliveryUpdateRequest.originAdrressId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryUpdateRequest.originAdrressId()));
            delivery.setOriginAdrress(addressOrigin);
        }

        if(deliveryUpdateRequest.destinationAddressId() != null){
            Address addressDestination = addressRepository.findById(deliveryUpdateRequest.destinationAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryUpdateRequest.destinationAddressId()));
            delivery.setDestinationAddress(addressDestination);
        }

        if(deliveryUpdateRequest.customerCollectsId() != null){
            Company customerCollects = companyRepository.findById(deliveryUpdateRequest.customerCollectsId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryUpdateRequest.customerCollectsId()));
            delivery.setCustomerCollects(customerCollects);
        }

        if(deliveryUpdateRequest.customerDeliveryId() != null){
            Company customerDelivery = companyRepository.findById(deliveryUpdateRequest.customerDeliveryId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryUpdateRequest.customerDeliveryId()));
            delivery.setCustomerDelivery(customerDelivery);
        }

        deliveryRepository.save(delivery);

        return new DeliveryCreateResponse(
                delivery.getId(),
                delivery.getWeight(),
                delivery.getVolume(),
                delivery.getScheduledCollection(),
                delivery.getScheduledDelivery(),
                delivery.getRoutePlanned(),
                delivery.getRouteCompleted(),
                delivery.getStatus(),
                delivery.getDeliverySequence(),
                delivery.getUser().getId(),
                delivery.getDeliveryType().getId(),
                delivery.getTransport().getId(),
                delivery.getTypeTransport().getId(),
                delivery.getOriginAdrress().getId(),
                delivery.getDestinationAddress().getId(),
                delivery.getCustomerCollects().getId(),
                delivery.getCustomerDelivery().getId()
        );
    }
}
