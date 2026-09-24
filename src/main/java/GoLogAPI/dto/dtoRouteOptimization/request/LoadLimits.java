package GoLogAPI.dto.dtoRouteOptimization.request;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public record LoadLimits(
        @JsonValue
        Map<String, LoadLimit> limits
) {
    public LoadLimits(Weight weight) {
        this(Map.of("weight", new LoadLimit(weight.maxLoad())));
    }

    public static LoadLimits of(Map<String, LoadLimit> limits) {
        return new LoadLimits(limits != null ? limits : new HashMap<>());
    }
}
