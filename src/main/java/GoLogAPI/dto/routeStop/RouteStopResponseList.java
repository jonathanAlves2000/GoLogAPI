package GoLogAPI.dto.routeStop;

import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.model.enums.RouteStopStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record RouteStopResponseList(
        UUID id,
        Integer sequenceOrder,
        String routePlanned,
        String routeCompleted,
        Double calculatedCost,
        Double realizedCots,
        Integer calculatedDistance,
        Integer realizedDistance,
        Integer calculatedDuration,
        Integer realizedDuration,
        Integer calculatedWait,
        Integer realizedWait,
        Transport transport,
        Shipment shipment,
        RouteStopStatus status,
        LocalDateTime arrivedAt,
        LocalDateTime departedAt,
        Boolean autoCheckIn,
        Double checkInDistance
) { }
