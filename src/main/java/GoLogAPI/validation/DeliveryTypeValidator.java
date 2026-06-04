package GoLogAPI.validation;

import GoLogAPI.dto.shipmentType.DeliveryTypeRequest;
import GoLogAPI.repository.ShipmentTypeRepository;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class DeliveryTypeValidator {

    private final ShipmentTypeRepository shipmentTypeRepository;

    public DeliveryTypeValidator(ShipmentTypeRepository deliveryTypeValidator){
        this.shipmentTypeRepository = deliveryTypeValidator;
    }

    public void validate(DeliveryTypeRequest deliveryTypeRequest){
        List<String> errors = new ArrayList<>();
       if(deliveryTypeRequest.name() != null && !deliveryTypeRequest.name().isBlank())
           name(deliveryTypeRequest.name(), errors);
    }

    public void name(String name, List<String> errors){
        boolean exists = shipmentTypeRepository.existsByName(name);
        if(exists) errors.add("Já existe esse nome cadastrado para o tipo de entrega.");
    }

}
