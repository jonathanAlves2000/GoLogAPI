package GoLogAPI.service;

import GoLogAPI.dto.shipmentType.DeliveryTypeCreateRequest;
import GoLogAPI.dto.shipmentType.DeliveryTypeResponse;
import GoLogAPI.dto.shipmentType.DeliveryTypeUpdateRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.ShipmentType;
import GoLogAPI.repository.ShipmentTypeRepository;
import GoLogAPI.validation.DeliveryTypeValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ShipmentTypeService {

    private final ShipmentTypeRepository shipmentTypeRepository;
    private final DeliveryTypeValidator deliveryTypeValidator;

    public ShipmentTypeService(ShipmentTypeRepository shipmentTypeRepository, DeliveryTypeValidator deliveryTypeValidator){
        this.deliveryTypeValidator = deliveryTypeValidator;
        this.shipmentTypeRepository = shipmentTypeRepository;
    }

    @Transactional
    public DeliveryTypeResponse save(DeliveryTypeCreateRequest deliveryTypeCreateRequest){

        deliveryTypeValidator.validate(deliveryTypeCreateRequest);

        ShipmentType shipmentType = ShipmentType.builder()
                .name(deliveryTypeCreateRequest.name())
                .description(deliveryTypeCreateRequest.description())
                .care(deliveryTypeCreateRequest.care())
                .build();

        shipmentType = shipmentTypeRepository.save(shipmentType);

        return new DeliveryTypeResponse(
                shipmentType.getId(),
                shipmentType.getName(),
                shipmentType.getDescription(),
                shipmentType.getCare()
        );
    }

    public DeliveryTypeResponse get(UUID id){
        ShipmentType shipmentType = shipmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        return new DeliveryTypeResponse(
                shipmentType.getId(),
                shipmentType.getName(),
                shipmentType.getDescription(),
                shipmentType.getCare()
        );
    }

    @Transactional
    public void delete(UUID id){
        ShipmentType shipmentType = shipmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        shipmentTypeRepository.delete(shipmentType);
    }

    @Transactional
    public DeliveryTypeResponse update(UUID id, DeliveryTypeCreateRequest deliveryTypeCreateRequest){

        ShipmentType shipmentType = shipmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        deliveryTypeValidator.validate(deliveryTypeCreateRequest);

       shipmentType.setName(deliveryTypeCreateRequest.name());
       shipmentType.setDescription(deliveryTypeCreateRequest.description());
       shipmentType.setCare(deliveryTypeCreateRequest.care());

       shipmentTypeRepository.save(shipmentType);

       return new DeliveryTypeResponse(
               shipmentType.getId(),
               shipmentType.getName(),
               shipmentType.getDescription(),
               shipmentType.getCare()
       );
    }

    @Transactional
    public DeliveryTypeResponse updatePartial(UUID id, DeliveryTypeUpdateRequest deliveryTypeUpdateRequest){
        ShipmentType shipmentType = shipmentTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        deliveryTypeValidator.validate(deliveryTypeUpdateRequest);

        if(deliveryTypeUpdateRequest.name() != null && !deliveryTypeUpdateRequest.name().isBlank())
            shipmentType.setName(deliveryTypeUpdateRequest.name());
        if(deliveryTypeUpdateRequest.description() != null && !deliveryTypeUpdateRequest.description().isBlank())
            shipmentType.setDescription(deliveryTypeUpdateRequest.description());
        if(deliveryTypeUpdateRequest.care() != null && !deliveryTypeUpdateRequest.care().isBlank())
            shipmentType.setCare(deliveryTypeUpdateRequest.care());

        shipmentTypeRepository.save(shipmentType);

        return new DeliveryTypeResponse(
                shipmentType.getId(),
                shipmentType.getName(),
                shipmentType.getDescription(),
                shipmentType.getCare()
        );
    }

}
