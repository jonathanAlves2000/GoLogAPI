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
        String routePlanned,
        String routeCompleted,
        Double calculatedDistance,
        Double calculatedDuration,
        String status,
        Integer calculatedWait,
        Double shippingSequence,
        Double calculatedCost,
        User user,
        ShipmentType shipmentType,
        Transport transport,
        TypeTransport typeTransport,
        Address address,
        Company customer,
        Shipment operationOrigem
) { }
