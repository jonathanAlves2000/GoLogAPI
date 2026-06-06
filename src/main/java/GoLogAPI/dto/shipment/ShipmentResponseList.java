package GoLogAPI.dto.shipment;

import GoLogAPI.model.*;
import jakarta.persistence.Column;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentResponseList(
        UUID id,
        TypeOperation typeOperation,
        Double weight,
        Double volume,
        LocalDateTime shedulind,
        String status,
        User user,
        ShipmentType shipmentType,
        TypeTransport typeTransport,
        Address address,
        Company customer,
        Shipment operationOrigem
) { }
