package GoLogAPI.dto.dtoRouteOptimization.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiRouteOptimizationResponse(
        List<ApiVehicleRoute> routes
) { }
