package GoLogAPI.dto.telemetry;

import java.time.LocalDateTime;
import java.util.UUID;

public record TelemetryBatchItemRequest(
        String plate,
        UUID equipamentId,
        String latitude,
        String longitude,
        Double speed,
        LocalDateTime dateTime,
        String alert,
        String device,
        String data1,
        String data2,
        // Opcionais: quando ausentes, a viagem em execução do veículo é resolvida automaticamente
        UUID shipmentId,
        UUID transportId
) { }
