package GoLogAPI.dto.dtoRouteOptimization;

import java.util.List;

public record Model(
        List<Vehicle> vehicles,
        List<RouteShipment> shipments,
        String globalStartTime,
        String globalEndTime
) { }
