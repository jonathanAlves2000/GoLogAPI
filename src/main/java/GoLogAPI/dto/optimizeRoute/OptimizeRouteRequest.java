package GoLogAPI.dto.optimizeRoute;

import GoLogAPI.model.enums.RoutePriority;

import java.util.List;
import java.util.UUID;

public record OptimizeRouteRequest(
        List<UUID> shipmentIds,
        List<UUID> workScheduleIds,
        RoutePriority routePriority,

        // Configuração de Regras / Perfil dinâmico
        UUID profileId,

        // Overrides pontuais opcionais da interface
        Double kmCostMultiplier,
        Double hourCostMultiplier,
        Double fixedCostPerVehicle,
        Double penaltyCostUnserved,
        Integer defaultServiceDurationSeconds,
        Integer timeWindowLeadMinutes
) {
    public OptimizeRouteRequest(List<UUID> shipmentIds, List<UUID> workScheduleIds, RoutePriority routePriority) {
        this(shipmentIds, workScheduleIds, routePriority, null, null, null, null, null, null, null);
    }
}
