package GoLogAPI.dto.optimizationProfile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OptimizationProfileCreateRequest(
        @NotBlank(message = "O nome do perfil é obrigatório")
        String name,

        String description,

        @NotNull(message = "O ID da empresa é obrigatório")
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
        Integer globalHorizonExtraDays
) { }
