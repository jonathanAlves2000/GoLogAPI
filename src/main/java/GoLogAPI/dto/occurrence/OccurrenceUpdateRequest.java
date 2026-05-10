package GoLogAPI.dto.occurrence;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record OccurrenceUpdateRequest(
        String type,

        @Size(min = 5, max = 500, message = "Descrição deve ter entre 5 e 500 caracteres.")
        String description,

        @Size(min = 5, max = 1000)
        String attachment,

        UUID deliveryId,

        UUID transportId,

        UUID senderId
) { }
