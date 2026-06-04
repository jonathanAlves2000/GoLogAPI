package GoLogAPI.dto.dtoRouteOptimization;

import java.util.List;

public record Vehicle(
        String label,
        Location startLocation,
        LoadLimits loadLimits,
        List<TimeWindow> startTimeWindows,
        List<TimeWindow> endTimeWindows,
        Double costPerKilometer
) { }
