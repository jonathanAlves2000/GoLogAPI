package GoLogAPI.service.telemetry;

import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.enums.TypeOperation;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Carga a bordo de uma viagem reconstruída pelas paradas já atendidas — mesma regra do t·km planejado
 * ({@code DashboardService.tonKm}): entregas sem coleta na própria viagem saem carregadas da origem; uma coleta
 * soma o peso das entregas vinculadas a ela (ou o próprio peso, se não houver); cada entrega descarrega.
 */
public final class TripLoad {

    private TripLoad() {
    }

    /** kg a bordo agora, considerando atendidas as paradas com chegada registrada. */
    public static double onboardKg(List<RouteStop> stops) {
        return onboardKgAt(stops, null);
    }

    /**
     * kg a bordo no instante {@code at}: só contam como atendidas as paradas com chegada até esse instante.
     * {@code null} considera todas as chegadas registradas.
     */
    public static double onboardKgAt(List<RouteStop> stops, LocalDateTime at) {
        Set<UUID> collectsInTrip = stops.stream().map(RouteStop::getShipment).filter(Objects::nonNull)
                .filter(s -> s.getTypeOperation() == TypeOperation.COLETA).map(Shipment::getId)
                .collect(Collectors.toSet());

        Map<UUID, Double> childWeightByCollect = new HashMap<>();
        double onboard = 0;
        for (RouteStop rs : stops) {
            Shipment s = rs.getShipment();
            if (s == null || s.getTypeOperation() != TypeOperation.ENTREGA) continue;
            if (s.getOperationOrigem() != null && collectsInTrip.contains(s.getOperationOrigem().getId())) {
                childWeightByCollect.merge(s.getOperationOrigem().getId(), nz(s.getWeight()), Double::sum);
            } else {
                onboard += nz(s.getWeight());
            }
        }

        for (RouteStop rs : stops) {
            Shipment s = rs.getShipment();
            if (s == null || rs.getArrivedAt() == null) continue;
            if (at != null && rs.getArrivedAt().isAfter(at)) continue;
            if (s.getTypeOperation() == TypeOperation.ENTREGA) {
                onboard = Math.max(0, onboard - nz(s.getWeight()));
            } else if (s.getTypeOperation() == TypeOperation.COLETA) {
                onboard += childWeightByCollect.getOrDefault(s.getId(), nz(s.getWeight()));
            }
        }
        return onboard;
    }

    private static double nz(Double v) {
        return v == null ? 0.0 : v;
    }
}
