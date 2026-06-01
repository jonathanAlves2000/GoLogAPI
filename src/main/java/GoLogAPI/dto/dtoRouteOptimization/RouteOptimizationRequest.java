package GoLogAPI.dto.dtoRouteOptimization;

public record RouteOptimizationRequest(
    Model model,
    String globalStartTime,
    String globalEndTime
) { }
