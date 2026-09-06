package GoLogAPI.dto.shipment;

import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.model.enums.TypeOperation;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentUpdateRequest(

        TypeOperation typeOperation,
        @Positive(message = "O peso deve ser positivo.")
        Double weight,
        @Positive(message = "O volume deve ser positivo.")
        Double volume,
        @FutureOrPresent(message = "A data de agendamento da remessa deve ser no futuro ou presente")
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
