package GoLogAPI.dto.shipment;

import GoLogAPI.model.TypeOperation;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentUpdateResponse(
        UUID id,
        TypeOperation typeOperation,
        Double weight,
        Double volume,
        LocalDateTime schedulind,
        String status,
        UUID userId,
        UUID shipmentTypeId,
        UUID typeTransportId,
        UUID addressId,
        UUID customerId,
        UUID operationOrigemId
) { }
