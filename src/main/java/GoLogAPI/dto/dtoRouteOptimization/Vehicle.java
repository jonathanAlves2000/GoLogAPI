package GoLogAPI.dto.dtoRouteOptimization;

public record Vehicle(
        String label,

        Location startLocation,

        LoadLimits loadLimits
) { }
