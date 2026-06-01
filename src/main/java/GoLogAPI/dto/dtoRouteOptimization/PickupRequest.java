package GoLogAPI.dto.dtoRouteOptimization;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record PickupRequest(
        @JsonUnwrapped
        Stop arrivalLocation,
        LoadDemands loadDemands
) {
}
