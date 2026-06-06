package GoLogAPI.dto.shipment;

import GoLogAPI.model.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentResponseListPersonalized(
        UUID id,
        TypeOperation typeOperation,
        Double weight,
        Double volume,
        LocalDateTime shedulind,
        String status,
        ShipmentType shipmentType,
        TypeTransport typeTransport,
        Company customer,
        Shipment operationOrigem,
        Transport transport,
        RouteStop routeStop
) { }
