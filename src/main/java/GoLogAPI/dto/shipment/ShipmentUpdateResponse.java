package GoLogAPI.dto.shipment;

import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.model.enums.TypeOperation;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentUpdateResponse(
        UUID id,
        TypeOperation typeOperation,
        Double weight,
        Double volume,
        LocalDateTime schedulind,
        ShipmentStatus status,
        UUID userId,
        UUID shipmentTypeId,
        UUID typeTransportId,
        UUID addressId,
        UUID customerId,
        UUID operationOrigemId
) { }
