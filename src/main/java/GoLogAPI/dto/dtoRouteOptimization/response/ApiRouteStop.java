package GoLogAPI.dto.dtoRouteOptimization.response;

public record ApiRouteStop(
        String shipmentLabel,
        boolean isPickup,
        String startTime
) { }
