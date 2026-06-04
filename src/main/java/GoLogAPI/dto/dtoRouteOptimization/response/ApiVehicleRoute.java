package GoLogAPI.dto.dtoRouteOptimization.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiVehicleRoute(
        String vehicleLabel,
        int vehicleIndex,
        List<ApiRouteStop> visits,
        List<ApiRouteTransition> transitions,
        ApiRouteMetrics metrics,
        Double routeTotalCost
) { }
