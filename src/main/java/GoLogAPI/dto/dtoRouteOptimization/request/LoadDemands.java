package GoLogAPI.dto.dtoRouteOptimization.request;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public record LoadDemands(
        @JsonValue
        Map<String, LoadDemand> demands
) {
    public LoadDemands(WeightAmount weight) {
        this(Map.of("weight", new LoadDemand(weight.amount())));
    }

    public static LoadDemands of(Map<String, LoadDemand> demands) {
        return new LoadDemands(demands != null ? demands : new HashMap<>());
    }
}
