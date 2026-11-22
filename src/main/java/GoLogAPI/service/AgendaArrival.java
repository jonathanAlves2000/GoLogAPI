package GoLogAPI.service;

import GoLogAPI.model.Occurrence;
import GoLogAPI.model.RouteStop;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Chegada usada no SLA de agenda, por remessa — mesma regra no dashboard e no acompanhamento da viagem.
 *
 * <ol>
 *     <li>Chegada na parada registrada pelo geofence (primeiro ponto de telemetria dentro do raio).</li>
 *     <li>Sem ela, o horário do registro de entrega (ocorrência "Fim" do motorista). É posterior à chegada real
 *     (vem depois da descarga), então pode marcar como atrasada uma entrega que chegou no horário.</li>
 * </ol>
 * A agenda é atendida quando a chegada é até o horário agendado; chegada antecipada conta como no prazo.
 */
public final class AgendaArrival {

    public enum Source { GEOFENCE, DELIVERY_RECORD }

    public record Arrival(LocalDateTime at, Source source) {
        public boolean onTime(LocalDateTime scheduled) {
            return !at.isAfter(scheduled);
        }
    }

    private AgendaArrival() {
    }

    public static Map<UUID, Arrival> resolve(Collection<RouteStop> stops, Collection<Occurrence> occurrences) {
        Map<UUID, Arrival> result = new HashMap<>();
        for (RouteStop rs : stops) {
            if (rs.getShipment() == null || rs.getShipment().getId() == null || rs.getArrivedAt() == null) continue;
            result.merge(rs.getShipment().getId(), new Arrival(rs.getArrivedAt(), Source.GEOFENCE), AgendaArrival::earliest);
        }
        if (occurrences != null) {
            for (Occurrence o : occurrences) {
                if (o.getShipment() == null || o.getShipment().getId() == null || o.getDateTime() == null || o.getType() == null) continue;
                if (!o.getType().trim().equalsIgnoreCase("Fim")) continue;
                UUID id = o.getShipment().getId();
                Arrival current = result.get(id);
                if (current != null && current.source() == Source.GEOFENCE) continue; // geofence tem prioridade
                Arrival record = new Arrival(o.getDateTime(), Source.DELIVERY_RECORD);
                result.put(id, current == null ? record : earliest(current, record));
            }
        }
        return result;
    }

    private static Arrival earliest(Arrival a, Arrival b) {
        return a.at().isAfter(b.at()) ? b : a;
    }
}
