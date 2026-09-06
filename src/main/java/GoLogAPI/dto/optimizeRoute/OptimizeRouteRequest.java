package GoLogAPI.dto.optimizeRoute;

import GoLogAPI.model.enums.RoutePriority;

import java.util.List;
import java.util.UUID;

public record OptimizeRouteRequest(
        List<UUID> shipmentIds,
        List<UUID> workScheduleIds,
        RoutePriority routePriority
) { }
