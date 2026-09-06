package GoLogAPI.validation;

import GoLogAPI.dto.shipment.ShipmentRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.Shipment;
import GoLogAPI.repository.ShipmentRepository;
import GoLogAPI.service.MessageException;
import org.springframework.stereotype.Component;


@Component
public class ShipementValidate {

    private final ShipmentRepository shipmentRepository;

    public ShipementValidate(ShipmentRepository shipmentRepository){
        this.shipmentRepository = shipmentRepository;
    }

    public void validate(ShipmentRequest shipmentRequest){

        Shipment shipmentCollect = shipmentRepository.findByOperationOrigemId(shipmentRequest.operationOrigemId());

        if(shipmentCollect == null)
            throw new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentRequest.operationOrigemId());

        if(shipmentRequest.schedulind().plusHours(2).isBefore(shipmentCollect.getSchedulind())) {
            throw new IllegalArgumentException("Data de agendamento da entrega deve ser no mínimo 2 horas após a data de agendamento da coleta");
        }
    }
}
