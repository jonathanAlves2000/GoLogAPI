package GoLogAPI.dto.shipment;

import GoLogAPI.model.TypeOperation;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentCreateRequest(

    @NotNull(message = "O tipo da remessa deve ser informada.")
    TypeOperation typeOperation,

    @NotNull(message = "O peso da remessa não poder nulo.")
    @PositiveOrZero(message = "O peso da remessa não pode ser menor que zero.")
    Double weight,

    @NotNull(message = "O volume da remessa não poder nulo.")
    @PositiveOrZero(message = "O volume da remessa não pode ser menor que zero.")
    Double volume,

    @NotNull(message = "A data de agendamento da remessa não poder nula.")
    @Future(message = "A data de agendamento da remessa deve ser no futuro")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    LocalDateTime schedulind,

    @Size(min = 1, max = 30, message = "O status da remessa deve ter entre 1 e 30 caracteres.")
    String status,

    @NotNull(message = "O Id so usuario não pode ser nulo.")
    UUID userId,

    @NotNull(message = "O Id do tipo de entrega não pode ser nulo.")
    UUID shipmentTypeId,

    @NotNull(message = "O Id do tipo de transporte não pode ser nulo.")
    UUID typeTransportId,

    @NotNull(message = "O Id do endereço da operação não pode ser nulo.")
    UUID addressId,

    @NotNull(message = "O Id do cliente da operação não pode ser nulo.")
    UUID customerId,

    UUID operationOrigemId
) { }
