package GoLogAPI.dto.dtoRouteOptimization;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record DeliveryRequest(
        @JsonUnwrapped
        Stop arrivalLocation,
        LoadDemands loadDemands
) { }
