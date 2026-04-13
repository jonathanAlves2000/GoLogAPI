package GoLogAPI.dto.telemetry;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TelemetryRequest {

    LocalDateTime dateTime();
    String latitude();
    String longitude();
    Double speed();
    String alert();
    String data1();
    String data2();
    String device();
    UUID equipamentId();
}
