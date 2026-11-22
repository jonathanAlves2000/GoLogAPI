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
        Integer globalHorizonExtraDays,
        @jakarta.validation.constraints.Min(value = 20, message = "O raio do geofence deve ser de no mínimo 20 m")
        @jakarta.validation.constraints.Max(value = 5000, message = "O raio do geofence deve ser de no máximo 5000 m")
        Integer geofenceRadiusMeters
) { }
