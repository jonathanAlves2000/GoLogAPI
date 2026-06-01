package GoLogAPI.dto.dtoRouteOptimization;

import java.util.List;

public record Stop(

        Location arrivalLocation,

        String duration,

        List<TimeWindow> timeWindows
) { }
