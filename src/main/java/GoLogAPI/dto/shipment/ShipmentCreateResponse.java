package GoLogAPI.dto.shipment;

import GoLogAPI.model.TypeOperation;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentCreateResponse(
        UUID id,
        TypeOperation typeOperation,
        Double weight,
        Double volume,
        LocalDateTime schedulind,
        String status,
        Integer shippingSequence,
        UUID userId,
        UUID shipmentTypeId,
        UUID transportId,
        UUID typeTransportId,
        UUID addressId,
        UUID customerId,
        UUID operationOrigemId
) { }
