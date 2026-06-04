package GoLogAPI.dto.dtoRouteOptimization.request;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record DeliveryRequest(
        @JsonUnwrapped
        Stop arrivalLocation,
        LoadDemands loadDemands
) { }
