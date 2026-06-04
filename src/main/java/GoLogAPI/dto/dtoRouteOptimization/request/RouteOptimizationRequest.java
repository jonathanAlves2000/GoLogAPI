package GoLogAPI.dto.dtoRouteOptimization.request;

public record RouteOptimizationRequest(
    Model model,
    Boolean populatePolylines,
    Boolean populateTransitionPolylines
) { }
