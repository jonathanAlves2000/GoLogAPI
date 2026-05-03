package GoLogAPI.service;

import GoLogAPI.dto.deliveryType.DeliveryTypeCreateRequest;
import GoLogAPI.dto.deliveryType.DeliveryTypeResponse;
import GoLogAPI.dto.deliveryType.DeliveryTypeUpdateRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.DeliveryType;
import GoLogAPI.repository.DeliveryTypeRepository;
import GoLogAPI.validation.DeliveryTypeValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DeliveryTypeService {

    private final DeliveryTypeRepository deliveryTypeRepository;
    private final DeliveryTypeValidator deliveryTypeValidator;

    public DeliveryTypeService(DeliveryTypeRepository deliveryTypeRepository, DeliveryTypeValidator deliveryTypeValidator){
        this.deliveryTypeValidator = deliveryTypeValidator;
        this.deliveryTypeRepository = deliveryTypeRepository;
    }

    @Transactional
    public DeliveryTypeResponse save(DeliveryTypeCreateRequest deliveryTypeCreateRequest){

        deliveryTypeValidator.validate(deliveryTypeCreateRequest);

        DeliveryType deliveryType = DeliveryType.builder()
                .name(deliveryTypeCreateRequest.name())
                .description(deliveryTypeCreateRequest.description())
                .care(deliveryTypeCreateRequest.care())
                .build();

        deliveryType = deliveryTypeRepository.save(deliveryType);

        return new DeliveryTypeResponse(
                deliveryType.getId(),
                deliveryType.getName(),
                deliveryType.getDescription(),
                deliveryType.getCare()
        );
    }

    public DeliveryTypeResponse get(UUID id){
        DeliveryType deliveryType = deliveryTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        return new DeliveryTypeResponse(
                deliveryType.getId(),
                deliveryType.getName(),
                deliveryType.getDescription(),
                deliveryType.getCare()
        );
    }

    @Transactional
    public void delete(UUID id){
        DeliveryType deliveryType = deliveryTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        deliveryTypeRepository.delete(deliveryType);
    }

    @Transactional
    public DeliveryTypeResponse update(UUID id, DeliveryTypeCreateRequest deliveryTypeCreateRequest){

        DeliveryType deliveryType = deliveryTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        deliveryTypeValidator.validate(deliveryTypeCreateRequest);

       deliveryType.setName(deliveryTypeCreateRequest.name());
       deliveryType.setDescription(deliveryTypeCreateRequest.description());
       deliveryType.setCare(deliveryTypeCreateRequest.care());

       deliveryTypeRepository.save(deliveryType);

       return new DeliveryTypeResponse(
               deliveryType.getId(),
               deliveryType.getName(),
               deliveryType.getDescription(),
               deliveryType.getCare()
       );
    }

    @Transactional
    public DeliveryTypeResponse updatePartial(UUID id, DeliveryTypeUpdateRequest deliveryTypeUpdateRequest){
        DeliveryType deliveryType = deliveryTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        deliveryTypeValidator.validate(deliveryTypeUpdateRequest);

        if(deliveryTypeUpdateRequest.name() != null && !deliveryTypeUpdateRequest.name().isBlank())
            deliveryType.setName(deliveryTypeUpdateRequest.name());
        if(deliveryTypeUpdateRequest.description() != null && !deliveryTypeUpdateRequest.description().isBlank())
            deliveryType.setDescription(deliveryTypeUpdateRequest.description());
        if(deliveryTypeUpdateRequest.care() != null && !deliveryTypeUpdateRequest.care().isBlank())
            deliveryType.setCare(deliveryTypeUpdateRequest.care());

        deliveryTypeRepository.save(deliveryType);

        return new DeliveryTypeResponse(
                deliveryType.getId(),
                deliveryType.getName(),
                deliveryType.getDescription(),
                deliveryType.getCare()
        );
    }

}
