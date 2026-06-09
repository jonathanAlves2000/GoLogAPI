package GoLogAPI.dto.occurrence;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record OccurrenceCreateRequest(
    @NotBlank(message = "Tipo de ocorrência é obrigatório")
    String type,

    @NotNull(message = "Data de criação é obrigatoria")
    LocalDateTime dateTime,

    @Size(min = 5, max = 500, message = "Descrição deve ter entre 5 e 500 caracteres.")
    String description,

    @Size(min = 5, max = 1000)
    String attachment,

    @NotNull(message = "O Id da entrega deve ser informado.")
    UUID shipmentId,

    @NotNull(message = "O Id do transporte deve ser informado.")
    UUID transportId,

    @NotNull(message = "O Id emissor da ocorrência deve ser informado.")
    UUID senderId
) { }
