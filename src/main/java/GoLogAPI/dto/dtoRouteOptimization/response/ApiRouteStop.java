package GoLogAPI.dto.dtoRouteOptimization.response;

import java.util.UUID;

public record ApiRouteStop(
        String shipmentLabel,
        boolean isPickup,
        String startTime
) { }
