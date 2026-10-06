package GoLogAPI.dto.dtoRouteOptimization.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record Stop(
        Location arrivalLocation,
        String duration,
        List<TimeWindow> timeWindows,
        List<String> visitTypes
) {
    public Stop(Location arrivalLocation, String duration, List<TimeWindow> timeWindows) {
        this(arrivalLocation, duration, timeWindows, null);
    }
}
