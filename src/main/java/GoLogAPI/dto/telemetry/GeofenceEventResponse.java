package GoLogAPI.dto.telemetry;

import GoLogAPI.model.enums.RouteStopStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record GeofenceEventResponse(
        String eventType,
        UUID routeStopId,
        UUID shipmentId,
        UUID transportId,
        String plate,
        Integer sequenceOrder,
        Double distanceMeters,
        RouteStopStatus status,
        LocalDateTime timestamp,
        String message
) { }
