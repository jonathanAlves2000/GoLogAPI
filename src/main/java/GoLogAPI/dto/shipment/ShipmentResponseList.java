package GoLogAPI.dto.shipment;

import GoLogAPI.model.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentResponseList(
        UUID id,
        TypeOperation typeOperation,
        Double weight,
        Double volume,
        LocalDateTime shedulind,
        String status,
        Integer shippingSequence,
        User user,
        ShipmentType shipmentType,
        Transport transport,
        TypeTransport typeTransport,
        Address address,
        Company customer,
        Shipment operationOrigem
) { }
