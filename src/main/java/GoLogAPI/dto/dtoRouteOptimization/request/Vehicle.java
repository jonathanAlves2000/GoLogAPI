package GoLogAPI.dto.dtoRouteOptimization.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Vehicle(
        String label,
        Location startLocation,
        LoadLimits loadLimits,
        List<TimeWindow> startTimeWindows,
        List<TimeWindow> endTimeWindows,
        Double costPerKilometer,
        Double costPerHour,
        Double fixedCost,
        Double costPerTraveledHour,
        Map<String, String> extraVisitDurationForVisitType
) {
    public Vehicle(
            String label,
            Location startLocation,
            LoadLimits loadLimits,
            List<TimeWindow> startTimeWindows,
            List<TimeWindow> endTimeWindows,
            Double costPerKilometer,
            Double costPerHour) {
        this(label, startLocation, loadLimits, startTimeWindows, endTimeWindows, costPerKilometer, costPerHour, null, null, null);
    }
}
