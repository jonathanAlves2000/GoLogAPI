package GoLogAPI.dto.shipment;

import GoLogAPI.model.*;
import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentResponse(
        UUID id,
        TypeOperation typeOperation,
        Double weight,
        Double volume,
        LocalDateTime schedulind,
        String status,
        Integer sequence,
        User user,
        ShipmentType shipmentType,
        Transport transport,
        TypeTransport typeTransport,
        Address address,
        Company customer,
        Shipment operationOrigem
) { }
