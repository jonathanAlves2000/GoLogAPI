package GoLogAPI.dto.dtoRouteOptimization.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record Model(
        List<Vehicle> vehicles,
        List<RouteShipment> shipments,
        String globalStartTime,
        String globalEndTime,
        List<ShipmentTypeIncompatibility> shipmentTypeIncompatibilities
) {
    public Model(List<Vehicle> vehicles, List<RouteShipment> shipments, String globalStartTime, String globalEndTime) {
        this(vehicles, shipments, globalStartTime, globalEndTime, null);
    }
}
