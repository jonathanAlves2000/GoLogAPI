package GoLogAPI.dto.dtoRouteOptimization.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiVehicleRoute(
        String vehicleLabel,
        int vehicleIndex,
        List<ApiRouteStop> visits,
        List<ApiRouteTransition> transitions,
        ApiRouteMetrics metrics,
        Double routeTotalCost,
        Map<String, Double> routeCosts
) { }
