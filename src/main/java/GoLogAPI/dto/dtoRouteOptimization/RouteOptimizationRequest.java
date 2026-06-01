package GoLogAPI.dto.dtoRouteOptimization;

import java.util.List;

public record RouteOptimizationRequest(
    Model model,
    Boolean populatePolylines
) { }
