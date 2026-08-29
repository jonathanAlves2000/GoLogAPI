package GoLogAPI.dto.shipment;

import GoLogAPI.model.*;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.model.enums.TypeOperation;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentResponse(
        UUID id,
        TypeOperation typeOperation,
        Double weight,
        Double volume,
        LocalDateTime shedulind,
        ShipmentStatus status,
        User user,
        ShipmentType shipmentType,
        TypeTransport typeTransport,
        Address address,
        Company customer,
        Shipment operationOrigem
) { }
