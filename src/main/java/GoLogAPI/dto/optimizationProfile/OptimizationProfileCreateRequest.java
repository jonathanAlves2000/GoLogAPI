package GoLogAPI.dto.optimizationProfile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OptimizationProfileCreateRequest(
        @NotBlank(message = "O nome do perfil é obrigatório")
        String name,

        String description,

        /** Opcional: quando omitido, o perfil pertence ao tenant ativo. */
        UUID companyId,

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
