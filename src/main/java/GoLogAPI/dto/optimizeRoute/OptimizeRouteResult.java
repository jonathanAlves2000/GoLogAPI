package GoLogAPI.dto.optimizeRoute;

import java.util.List;
import java.util.UUID;

/**
 * Resumo da roteirização devolvido ao front-end.
 *
 * @param routesCount        veículos que receberam ao menos uma visita
 * @param plannedShipments   remessas que entraram em alguma rota
 * @param transportIds       transportes gerados
 * @param skipped            remessas que o otimizador não conseguiu atender, com o motivo
 * @param totalDistanceMeters distância total planejada
 * @param totalCost          custo total estimado pelo otimizador
 */
public record OptimizeRouteResult(
        int routesCount,
        int plannedShipments,
        List<UUID> transportIds,
        List<SkippedShipment> skipped,
        long totalDistanceMeters,
        double totalCost
) {
    public record SkippedShipment(List<UUID> shipmentIds, String label, List<String> reasons) { }
}
