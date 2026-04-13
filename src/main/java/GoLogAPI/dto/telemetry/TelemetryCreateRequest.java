package GoLogAPI.dto.telemetry;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TelemetryCreateRequest(
        @NotNull(message = "A data não pode estar nula!")
        LocalDateTime dateTime,

        @NotBlank(message = "A latitude não pode estar nula!")
        String latitude,

        @NotBlank(message = "A longitude não pode estar nula!")
        String longitude,

        @NotNull(message = "A velocidade não pode estar nula!")
        Double speed,

        String alert,

        @NotBlank(message = "O dado 1 não pode estar nulo!")
        String data1,

        String data2,

        String device,

        @NotNull(message = "O ID do equipamento não estar ser nulo!")
        UUID equipamentId
) { }
