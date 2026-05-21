package GoLogAPI.dto.telemetry;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TelemetryCreateRequest(
        @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
        @NotNull(message = "Data da telemetria deve ser informada.")
        LocalDateTime dateTime,

        @NotBlank(message = "Latitude deve ser informada.")
        String latitude,

        @NotBlank(message = "Longitude deve ser informada.")
        String longitude,

        @NotNull(message = "Velocidade deve ser informada.")
        Double speed,

        String alert,

        @NotBlank(message = "Dado um deve ser informado.")
        String data1,

        String data2,

        String device,

        @NotNull(message = "O ID do equipamento deve ser informado.")
        UUID equipamentId
) implements TelemetryRequest{ }
