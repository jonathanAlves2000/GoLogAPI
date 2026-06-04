package GoLogAPI.dto.dtoRouteOptimization.request;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record PickupRequest(
        @JsonUnwrapped
        Stop arrivalLocation,
        LoadDemands loadDemands
) {
}
