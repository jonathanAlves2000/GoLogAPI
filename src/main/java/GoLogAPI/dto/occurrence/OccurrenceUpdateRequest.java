package GoLogAPI.dto.occurrence;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Size;

public record OccurrenceUpdateRequest(
        String type,

        LocalDateTime dateTime,

        @Size(min = 5, max = 500, message = "Descrição deve ter entre 5 e 500 caracteres.")
        String description,

        @Size(min = 5, max = 1000)
        String attachment,

        UUID shipmentId,

        UUID transportId,

        UUID senderId
) { }
