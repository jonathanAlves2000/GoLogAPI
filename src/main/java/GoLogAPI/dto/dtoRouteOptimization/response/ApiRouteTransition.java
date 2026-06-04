package GoLogAPI.dto.dtoRouteOptimization.response;

public record ApiRouteTransition(
        String travelDuration,
        Integer travelDistanceMeters,
        String waitDuration,
        GooglePolyline routePolyline
) { }
