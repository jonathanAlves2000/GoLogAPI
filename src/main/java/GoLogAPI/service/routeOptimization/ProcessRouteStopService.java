package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.optimizeRoute.OptimizeRouteRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.model.enums.RoutePriority;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.model.enums.TypeOperation;
import GoLogAPI.repository.RouteStopRepository;
import GoLogAPI.repository.ShipmentRepository;
import GoLogAPI.repository.TransportRepository;
import com.google.maps.routeoptimization.v1.ShipmentRoute;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Persiste o resultado do otimizador: um {@link Transport} por veículo usado e uma {@link RouteStop} por
 * visita, na ordem devolvida pelo Google.
 *
 * <p>Correções em relação à versão anterior:</p>
 * <ul>
 *   <li>Visitas cuja transição não tem polilinha (ex.: coleta e entrega no mesmo endereço, ou paradas
 *       consecutivas no mesmo local) eram descartadas — a remessa ficava "Aguardando início" sem parada.
 *       Agora toda visita vira parada.</li>
 *   <li>Replanejar uma remessa que já estava em outro transporte ainda não iniciado remove a parada antiga
 *       (e o transporte antigo, se ficar vazio), evitando paradas duplicadas.</li>
 * </ul>
 */
@Service
@Transactional
public class ProcessRouteStopService {

    private final ShipmentRepository shipmentRepository;
    private final ProcessTransportService processTransportService;
    private final RouteStopRepository routeStopRepository;
    private final TransportRepository transportRepository;

    private GoLogAPI.service.webhook.ShipmentEventPublisher shipmentEventPublisher;

    @org.springframework.beans.factory.annotation.Autowired
    void setShipmentEventPublisher(GoLogAPI.service.webhook.ShipmentEventPublisher shipmentEventPublisher) {
        this.shipmentEventPublisher = shipmentEventPublisher;
    }

    public ProcessRouteStopService(
            ShipmentRepository shipmentRepository,
            ProcessTransportService processTransportService,
            RouteStopRepository routeStopRepository,
            TransportRepository transportRepository) {
        this.shipmentRepository = shipmentRepository;
        this.processTransportService = processTransportService;
        this.routeStopRepository = routeStopRepository;
        this.transportRepository = transportRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public List<Transport> processShipment(List<ShipmentRoute> routes, RoutePriority routePriority) {
        return processShipment(routes, new OptimizeRouteRequest(null, null, routePriority));
    }

    /** @return transportes criados/atualizados (um por veículo com visitas) */
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public List<Transport> processShipment(List<ShipmentRoute> routes, OptimizeRouteRequest optimizeRouteRequest) {
        List<Transport> created = new ArrayList<>();
        for (ShipmentRoute vehicleRoute : routes) {
            if (vehicleRoute.getVisitsList().isEmpty()) {
                continue;
            }

            Transport transport = processTransportService.processTransport(vehicleRoute, optimizeRouteRequest);
            if (transport == null) {
                continue;
            }
            created.add(transport);

            routeStopRepository.deleteByTransportId(transport.getId());
            routeStopRepository.flush();

            List<ShipmentRoute.Visit> visits = vehicleRoute.getVisitsList();
            List<ShipmentRoute.Transition> transitions = vehicleRoute.getTransitionsList();

            List<UUID> targetIds = new ArrayList<>();
            for (ShipmentRoute.Visit visit : visits) {
                targetIds.add(resolveTargetId(visit));
            }

            Map<UUID, Shipment> shipmentMap = new HashMap<>();
            shipmentRepository.findAllById(targetIds).forEach(s -> shipmentMap.put(s.getId(), s));

            releaseFromPreviousTransports(new HashSet<>(targetIds), transport);

            List<RouteStop> newStops = new ArrayList<>();
            Map<UUID, Shipment> updatedShipments = new LinkedHashMap<>();
            for (int i = 0; i < visits.size(); i++) {
                // A transição i é o trajeto que chega na visita i
                ShipmentRoute.Transition transition = i < transitions.size() ? transitions.get(i) : null;

                UUID targetId = targetIds.get(i);
                Shipment shipment = shipmentMap.get(targetId);
                if (shipment == null) {
                    throw new ResourceNotFoundException("Remessa devolvida pelo otimizador não encontrada: ", targetId);
                }

                RouteStop stop = new RouteStop();
                stop.setSequenceOrder(i + 1);
                stop.setRoutePlanned(encodePolyline(transition));
                stop.setCalculatedCost(0.0);
                stop.setCalculatedDistance(transition != null ? (int) transition.getTravelDistanceMeters() : 0);
                stop.setCalculatedDuration(transition != null ? (int) transition.getTotalDuration().getSeconds() : 0);
                stop.setCalculatedWait(transition != null ? (int) transition.getWaitDuration().getSeconds() : 0);
                stop.setTransport(transport);
                stop.setShipment(shipment);

                shipment.setStatus(ShipmentStatus.AGUARDANDO_INICIO);
                updatedShipments.put(shipment.getId(), shipment);
                newStops.add(stop);
            }

            updateWeightAndVolume(newStops);

            if (!newStops.isEmpty()) {
                routeStopRepository.saveAll(newStops);
            }
            if (!updatedShipments.isEmpty()) {
                shipmentRepository.saveAll(updatedShipments.values());
            }
            for (RouteStop stop : newStops) {
                shipmentEventPublisher.publish("SHIPMENT_PLANNED", stop.getShipment(), stop, null);
            }
        }
        return created;
    }

    /**
     * Rótulos gerados em {@code RouteRequestService}:
     * {@code coletaId/entregaId} (par), {@code pickup/coletaId} (coleta avulsa) e {@code delivery/entregaId}.
     */
    static UUID resolveTargetId(ShipmentRoute.Visit visit) {
        String label = visit.getShipmentLabel();
        if (label == null || label.isBlank()) {
            throw new IllegalStateException("Visita do otimizador sem rótulo de remessa.");
        }
        if (!label.contains("/")) {
            return UUID.fromString(label);
        }
        String[] ids = label.split("/");
        String head = ids[0];
        if (head.equalsIgnoreCase("standalone") || head.equalsIgnoreCase("delivery") || head.equalsIgnoreCase("pickup")) {
            return UUID.fromString(ids[1]);
        }
        return visit.getIsPickup() ? UUID.fromString(ids[0]) : UUID.fromString(ids[1]);
    }

    private static String encodePolyline(ShipmentRoute.Transition transition) {
        if (transition == null || !transition.hasRoutePolyline() || transition.getRoutePolyline().getPoints().isEmpty()) {
            return "";
        }
        List<LatLng> points = OptimizeListLocation.procesingRouteGoogle(transition.getRoutePolyline().getPoints());
        return points == null || points.isEmpty() ? "" : com.google.maps.internal.PolylineEncoding.encode(points);
    }

    /** Remove paradas destas remessas em outros transportes ainda não iniciados. */
    private void releaseFromPreviousTransports(Set<UUID> shipmentIds, Transport current) {
        Set<Transport> touched = new HashSet<>();
        for (UUID shipmentId : shipmentIds) {
            for (RouteStop old : routeStopRepository.findByShipmentId(shipmentId)) {
                Transport oldTransport = old.getTransport();
                if (oldTransport == null || oldTransport.getId().equals(current.getId())) continue;
                if (oldTransport.getDistanceTraveled() != null) continue; // transporte já executado: histórico
                routeStopRepository.delete(old);
                touched.add(oldTransport);
            }
        }
        routeStopRepository.flush();
        for (Transport t : touched) {
            List<RouteStop> remaining = routeStopRepository.findByTransportId(t.getId());
            if (remaining.isEmpty()) {
                transportRepository.delete(t);
            } else {
                t.setShipmentQuantity(remaining.size());
                transportRepository.save(t);
            }
        }
    }

    private void updateWeightAndVolume(List<RouteStop> stops) {
        Map<UUID, Double> weightPerCollect = new HashMap<>();
        Map<UUID, Double> volumePerCollect = new HashMap<>();

        for (RouteStop stop : stops) {
            Shipment shipment = stop.getShipment();
            if (shipment.getTypeOperation() == TypeOperation.ENTREGA && shipment.getOperationOrigem() != null) {
                UUID collectId = shipment.getOperationOrigem().getId();
                weightPerCollect.merge(collectId, shipment.getWeight() != null ? shipment.getWeight() : 0.0, Double::sum);
                volumePerCollect.merge(collectId, shipment.getVolume() != null ? shipment.getVolume() : 0.0, Double::sum);
            }
        }

        for (RouteStop stop : stops) {
            Shipment shipment = stop.getShipment();
            if (shipment.getTypeOperation() == TypeOperation.COLETA && weightPerCollect.containsKey(shipment.getId())) {
                stop.setWeight(weightPerCollect.get(shipment.getId()));
                stop.setVolume(volumePerCollect.get(shipment.getId()));
            } else {
                stop.setWeight(shipment.getWeight() != null ? shipment.getWeight() : 0.0);
                stop.setVolume(shipment.getVolume() != null ? shipment.getVolume() : 0.0);
            }
        }
    }
}
