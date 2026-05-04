package GoLogAPI.dto.telemetry;

import java.time.LocalDateTime;
import java.util.UUID;

public record TelemetryResponseList(
        UUID id,
        LocalDateTime dateTime,
        String latitude,
        String longitude,
        Double speed,
        String alert,
        String data1,
        String data2,
        String device
) {
}
