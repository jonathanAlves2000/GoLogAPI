package GoLogAPI.service.telemetry;

import GoLogAPI.model.Equipament;
import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.repository.RouteStopRepository;
import GoLogAPI.repository.TransportRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Decide a qual viagem um ponto de telemetria pertence. Usado pela rota realizada e pelo geofencing, para que os
 * dois sempre concordem — um veículo pode ter várias viagens (concluídas, em curso e futuras).
 *
 * <ol>
 *     <li><b>Viagem informada no ponto</b> ({@code transportId}/{@code shipmentId} enviado pelo app): é a viagem
 *     que o motorista ativou, então vale ela, desde que ainda esteja aberta (tem remessa não finalizada). Não exige
 *     que o backend já saiba que ela começou: o status das remessas e a telemetria sobem por filas diferentes no
 *     app, e os primeiros pontos podem chegar antes do "Iniciado".</li>
 *     <li><b>Sem viagem informada</b>: a viagem em execução do veículo (a criada por último, se houver mais de
 *     uma). Nunca simplesmente a mais recente: ela pode ser uma viagem futura, ainda aguardando início.</li>
 *     <li>Se {@code allowNotStarted}, e nenhuma estiver em execução: a viagem aberta mais recente. O geofencing
 *     usa isso para o check-in automático na primeira parada iniciar a viagem.</li>
 * </ol>
 */
@Component
public class ActiveTripResolver {

    public record ResolvedTrip(Transport transport, List<RouteStop> stops) {}

    private final TransportRepository transportRepository;
    private final RouteStopRepository routeStopRepository;

    public ActiveTripResolver(TransportRepository transportRepository, RouteStopRepository routeStopRepository) {
        this.transportRepository = transportRepository;
        this.routeStopRepository = routeStopRepository;
    }

    /** @return a viagem do ponto, ou {@code null} se ele não pertence a nenhuma viagem aberta */
    public ResolvedTrip resolve(Equipament equipament, Transport linked, boolean allowNotStarted) {
        if (linked != null && linked.getId() != null) {
            List<RouteStop> stops = routeStopRepository.findByTransportIdOrderBySequenceOrderAsc(linked.getId());
            return isOpen(stops) ? new ResolvedTrip(linked, stops) : null;
        }

        if (equipament == null || equipament.getId() == null) return null;
        List<Transport> transports = transportRepository.findByEquipament1IdOrderByCreatedAtDesc(equipament.getId());
        if (transports.isEmpty() && equipament.getPlate() != null) {
            transports = transportRepository.findByEquipament1PlateOrderByCreatedAtDesc(equipament.getPlate());
        }

        ResolvedTrip newestOpen = null;
        for (Transport candidate : transports) {
            List<RouteStop> stops = routeStopRepository.findByTransportIdOrderBySequenceOrderAsc(candidate.getId());
            if (isRunning(stops)) return new ResolvedTrip(candidate, stops);
            if (newestOpen == null && isOpen(stops)) newestOpen = new ResolvedTrip(candidate, stops);
        }
        return allowNotStarted ? newestOpen : null;
    }

    /** Aberta: tem remessa roteirizada e nem todas estão finalizadas. */
    static boolean isOpen(List<RouteStop> stops) {
        List<Shipment> shipments = shipments(stops);
        return !shipments.isEmpty() && shipments.stream().anyMatch(s -> s.getStatus() != ShipmentStatus.FINALIZADO);
    }

    /** Em execução: aberta e já começou (há parada atendida ou remessa iniciada/finalizada). */
    static boolean isRunning(List<RouteStop> stops) {
        if (!isOpen(stops)) return false;
        return stops.stream().anyMatch(rs -> rs.getArrivedAt() != null)
                || shipments(stops).stream().anyMatch(s -> s.getStatus() == ShipmentStatus.INICIADO
                || s.getStatus() == ShipmentStatus.FINALIZADO);
    }

    private static List<Shipment> shipments(List<RouteStop> stops) {
        return stops.stream().map(RouteStop::getShipment).filter(Objects::nonNull).toList();
    }
}
