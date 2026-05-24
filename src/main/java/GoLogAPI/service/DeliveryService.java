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
    private final CollectRepository collectRepository;

    public DeliveryService(
            DeliveryRepository deliveryRepository, AddressRepository addressRepository,
            CompanyRepository companyRepository, UserRepository userRepository,
            DeliveryTypeRepository deliveryTypeRepository, TypeTransportRepository typeTransportRepository,
            TransportRepository transportRepository, CollectRepository collectRepository)
    {
        this.deliveryRepository = deliveryRepository;
        this.addressRepository = addressRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.deliveryTypeRepository = deliveryTypeRepository;
        this.typeTransportRepository = typeTransportRepository;
        this.transportRepository = transportRepository;
        this.collectRepository = collectRepository;
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

        Address deliveryAddress = addressRepository.findById(deliveryCreateRequest.deliveryAddressId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.deliveryAddressId()));

        Company customerDelivery = companyRepository.findById(deliveryCreateRequest.customerDeliveryId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.customerDeliveryId()));

        Collect collect = collectRepository.findById(deliveryCreateRequest.collectId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.collectId()));

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
                .deliveryAddress(deliveryAddress)
                .customerDelivery(customerDelivery)
                .collect(collect)
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
                delivery.getDeliveryAddress().getId(),
                delivery.getCustomerDelivery().getId(),
                delivery.getCollect().getId()
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
                delivery.getDeliveryAddress(),
                delivery.getCustomerDelivery(),
                delivery.getCollect()
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
                        delivery.getUser(),
                        delivery.getDeliveryType(),
                        delivery.getTransport(),
                        delivery.getTypeTransport(),
                        delivery.getDeliveryAddress(),
                        delivery.getCustomerDelivery(),
                        delivery.getCollect()
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

        Address deliveryAddress = addressRepository.findById(deliveryCreateRequest.deliveryAddressId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.deliveryAddressId()));

        Company customerDelivery = companyRepository.findById(deliveryCreateRequest.customerDeliveryId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.customerDeliveryId()));

        Collect collect = collectRepository.findById(deliveryCreateRequest.collectId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryCreateRequest.collectId()));

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
        delivery.setDeliveryAddress(deliveryAddress);
        delivery.setCustomerDelivery(customerDelivery);
        delivery.setCollect(collect);

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
                delivery.getDeliveryAddress().getId(),
                delivery.getCustomerDelivery().getId(),
                delivery.getCollect().getId()
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

        if(deliveryUpdateRequest.deliveryAddressId() != null){
            Address addressDestination = addressRepository.findById(deliveryUpdateRequest.deliveryAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryUpdateRequest.deliveryAddressId()));
            delivery.setDeliveryAddress(addressDestination);
        }

        if(deliveryUpdateRequest.customerDeliveryId() != null){
            Company customerDelivery = companyRepository.findById(deliveryUpdateRequest.customerDeliveryId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryUpdateRequest.customerDeliveryId()));
            delivery.setCustomerDelivery(customerDelivery);
        }

        if(deliveryUpdateRequest.collectId() != null){
            Collect collect = collectRepository.findById(deliveryUpdateRequest.collectId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, deliveryUpdateRequest.collectId()));
            delivery.setCollect(collect);
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
                delivery.getDeliveryAddress().getId(),
                delivery.getCustomerDelivery().getId(),
                delivery.getCollect() != null ? delivery.getCollect().getId() : null
        );
    }
}
