package GoLogAPI.dto.dtoRouteOptimization.request;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoadDemand(
        String amount
) { }
