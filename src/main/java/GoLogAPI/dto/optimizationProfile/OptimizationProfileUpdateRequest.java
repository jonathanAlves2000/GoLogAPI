package GoLogAPI.dto.optimizationProfile;

public record OptimizationProfileUpdateRequest(
        String name,
        String description,
        Boolean isDefault,

        Double kmCostMultiplier,
        Double hourCostMultiplier,
        Double fixedCostPerVehicle,
        Double costPerTraveledHour,
        Double penaltyCostUnserved,
        Double lateArrivalCostPerHour,

        Integer defaultServiceDurationSeconds,
        Integer timeWindowLeadMinutes,
        Integer vehicleStartWindowLeadHours,
        Integer vehicleEndWindowMarginHours,
        Integer globalHorizonExtraDays
) { }
