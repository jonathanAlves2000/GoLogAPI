package GoLogAPI.dto.dtoRouteOptimization;

import java.util.List;

public record ShipmentDto(
        String label,

        List<Stop> pickups,

        List<Stop> deliveries,

        LoadDemands loadDemands
) { }
