package GoLogAPI.dto.shipment;

import GoLogAPI.model.TypeOperation;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentUpdateRequest(

        TypeOperation typeOperation,
        @Positive
        Double weight,
        @Positive
        Double volume,
        @FutureOrPresent
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime schedulind,
        @Size(min = 1, max = 10, message = "O Status da entrega deve ter entre 1 e 10 caracteres.")
        String status,
        Integer shippingSequence,
        UUID userId,
        UUID shipmentTypeId,
        UUID transportId,
        UUID typeTransportId,
        UUID addressId,
        UUID customerId,
        UUID operationOrigemId
) {
}
