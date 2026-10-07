package GoLogAPI.dto.telemetry;

import java.util.List;

public record TelemetryBatchResponse(
        int totalReceived,
        int successCount,
        int errorCount,
        List<String> errors,
        List<GeofenceEventResponse> eventsDetected,
        long processingTimeMs
) { }
