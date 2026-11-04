package GoLogAPI.dto.shipment;

import java.time.LocalDateTime;
import java.util.UUID;

import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.model.enums.TypeOperation;
import jakarta.validation.constraints.PositiveOrZero;

public record ShipmentUpdateRequest(

        TypeOperation typeOperation,
        @PositiveOrZero(message = "O peso deve ser positivo ou zero.")
        Double weight,
        @PositiveOrZero(message = "O volume deve ser positivo ou zero.")
        Double volume,
        LocalDateTime schedulind,
        ShipmentStatus status,
        Integer shippingSequence,
        UUID userId,
        UUID shipmentTypeId,
        UUID transportId,
        UUID typeTransportId,
        UUID addressId,
        UUID customerId,
        UUID operationOrigemId
) implements ShipmentRequest{ }
