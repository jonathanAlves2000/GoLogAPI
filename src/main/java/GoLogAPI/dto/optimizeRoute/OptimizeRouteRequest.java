package GoLogAPI.dto.optimizeRoute;

import java.util.List;
import java.util.UUID;

public record OptimizeRouteRequest(
        List<UUID> shipmentIds,
        List<UUID> workScheduleIds
) { }
