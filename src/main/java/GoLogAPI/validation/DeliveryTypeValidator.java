package GoLogAPI.validation;

import GoLogAPI.dto.deliveryType.DeliveryTypeRequest;
import GoLogAPI.repository.DeliveryTypeRepository;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class DeliveryTypeValidator {

    private final DeliveryTypeRepository deliveryTypeRepository;

    public DeliveryTypeValidator(DeliveryTypeRepository deliveryTypeValidator){
        this.deliveryTypeRepository = deliveryTypeValidator;
    }

    public void validate(DeliveryTypeRequest deliveryTypeRequest){
        List<String> errors = new ArrayList<>();
        name(deliveryTypeRequest.name(), errors);
    }

    public void name(String name, List<String> errors){
        boolean exists = deliveryTypeRepository.existsByName(name);
        if(exists) errors.add("Já existe esse nome cadastrado para o tipo de entrega.");
    }

}
