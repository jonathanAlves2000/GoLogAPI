package GoLogAPI.dto.dtoRouteOptimization.request;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoadLimit(
        String maxLoad,
        String softMaxLoad,
        Double costPerUnitAboveSoftMax
) {
    public LoadLimit(String maxLoad) {
        this(maxLoad, null, null);
    }
}
