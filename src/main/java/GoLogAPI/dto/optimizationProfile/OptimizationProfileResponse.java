package GoLogAPI.dto.optimizationProfile;

import java.util.UUID;

public record OptimizationProfileResponse(
        UUID id,
        UUID companyId,
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
