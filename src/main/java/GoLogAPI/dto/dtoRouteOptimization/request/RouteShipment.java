package GoLogAPI.dto.dtoRouteOptimization.request;

import java.util.List;

public record RouteShipment(
        String label,
        List<PickupRequest> pickups,
        List<DeliveryRequest> deliveries,
        Double penaltyCost
) { }
