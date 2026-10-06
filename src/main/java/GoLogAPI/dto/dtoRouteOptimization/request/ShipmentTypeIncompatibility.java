package GoLogAPI.dto.dtoRouteOptimization.request;

import java.util.List;

public record ShipmentTypeIncompatibility(
        List<String> types,
        String incompatibilityMode
) {
    public static final String NOT_PERFORMED_BY_SAME_VEHICLE = "NOT_PERFORMED_BY_SAME_VEHICLE";
}
