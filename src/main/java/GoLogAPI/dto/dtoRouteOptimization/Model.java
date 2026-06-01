package GoLogAPI.dto.dtoRouteOptimization;

import java.util.List;

public record Model(
        List<Vehicle> vehicles,
        List<Shipment> shipments
) { }
