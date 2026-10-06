package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.optimizeRoute.OptimizeRouteRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.model.enums.RoutePriority;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.repository.RouteStopRepository;
import GoLogAPI.repository.ShipmentRepository;
import GoLogAPI.service.MessageException;
import com.google.cloud.optimization.v1.ShipmentRoute;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class ProcessRouteStopService {

    private final ShipmentRepository shipmentRepository;
    private final ProcessTransportService processTransportService;
    private final RouteStopRepository routeStopRepository;

    public ProcessRouteStopService(
            ShipmentRepository shipmentRepository,
            ProcessTransportService processTransportService,
            RouteStopRepository routeStopRepository) {
        this.shipmentRepository = shipmentRepository;
        this.processTransportService = processTransportService;
        this.routeStopRepository = routeStopRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void processShipment(List<ShipmentRoute> routes, RoutePriority routePriority) {
        processShipment(routes, new OptimizeRouteRequest(null, null, routePriority));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void processShipment(List<ShipmentRoute> routes, OptimizeRouteRequest optimizeRouteRequest) {
        for (ShipmentRoute vehicleRoute : routes) {

            if (vehicleRoute.getTransitionsList().isEmpty()) {
                continue;
            }

            Transport transport = processTransportService.processTransport(vehicleRoute, optimizeRouteRequest);
            if (transport == null) {
                continue;
            }

            routeStopRepository.deleteByTransportId(transport.getId());
            routeStopRepository.flush();

            double distanceTotal = vehicleRoute.hasMetrics() ? vehicleRoute.getMetrics().getTravelDistanceMeters() : 0.0;

            List<ShipmentRoute.Visit> visits = vehicleRoute.getVisitsList();
            List<ShipmentRoute.Transition> transitions = vehicleRoute.getTransitionsList();
            List<RouteStop> newStops = new ArrayList<>();
            List<Shipment> updatedShipments = new ArrayList<>();

            // 1. Coleta antecipada de todos os IDs de Shipment para busca em lote (evita N+1 findById)
            List<UUID> targetIds = new ArrayList<>();
            for (ShipmentRoute.Visit visit : visits) {
                String[] ids = visit.getShipmentLabel().split("/");
                UUID collectId = UUID.fromString(ids[0]);
                UUID deliveryId = UUID.fromString(ids[1]);
                UUID targetId = (visit.getVisitLabel() != null && visit.getVisitLabel().contains("pickup")) || visit.getIsPickup()
                        ? collectId : deliveryId;
                targetIds.add(targetId);
            }

            Map<UUID, Shipment> shipmentMap = new HashMap<>();
            shipmentRepository.findAllById(targetIds).forEach(s -> shipmentMap.put(s.getId(), s));

            int limit = Math.min(transitions.size(), visits.size());
            for (int i = 0; i < limit; i++) {
                ShipmentRoute.Transition routeTransition = transitions.get(i);

                if (!routeTransition.hasRoutePolyline() || routeTransition.getRoutePolyline().getPoints().isEmpty()) {
                    continue;
                }

                int duration = (int) routeTransition.getTotalDuration().getSeconds();
                int waitDuration = (int) routeTransition.getWaitDuration().getSeconds();
                int travelDistanceMeters = (int) routeTransition.getTravelDistanceMeters();

                List<LatLng> optimizedPoints = OptimizeListLocation.procesingRouteGoogle(routeTransition.getRoutePolyline().getPoints());
                String polylineCode = com.google.maps.internal.PolylineEncoding.encode(optimizedPoints);

                double costRoute = 0.0;
                if (distanceTotal > 0) {
                    costRoute = (travelDistanceMeters / distanceTotal) * 0.0;
                }

                int sequenceOrder = i + 1;
                UUID targetId = targetIds.get(i);
                Shipment shipment = shipmentMap.get(targetId);

                if (shipment == null) {
                    throw new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, targetId);
                }

                RouteStop stop = new RouteStop();
                stop.setSequenceOrder(sequenceOrder);
                stop.setRoutePlanned(polylineCode);
                stop.setCalculatedCost(costRoute);
                stop.setCalculatedDistance(travelDistanceMeters);
                stop.setCalculatedDuration(duration);
                stop.setCalculatedWait(waitDuration);
                stop.setTransport(transport);
                stop.setShipment(shipment);

                shipment.setStatus(ShipmentStatus.AGUARDANDO_INICIO);
                updatedShipments.add(shipment);
                newStops.add(stop);
            }

            // Atualiza pesos e volumes calculados
            this.updateWeightAndVolume(newStops);

            // 2. Operações em lote (Batch Save): salva todas as paradas e carregamentos de uma vez só!
            if (!newStops.isEmpty()) {
                routeStopRepository.saveAll(newStops);
            }
            if (!updatedShipments.isEmpty()) {
                shipmentRepository.saveAll(updatedShipments);
            }
        }
    }

    private void updateWeightAndVolume(List<RouteStop> stops) {
        if (stops == null || stops.isEmpty()) {
            return;
        }

        Map<UUID, Double> weightPerCollect = new HashMap<>();
        Map<UUID, Double> volumePerCollect = new HashMap<>();

        for (RouteStop stop : stops) {
            Shipment shipment = stop.getShipment();
            if (shipment != null && shipment.getTypeOperation() != null) {
                String typeStr = shipment.getTypeOperation().toString().trim();

                if ("ENTREGA".equalsIgnoreCase(typeStr) && shipment.getOperationOrigem() != null) {
                    UUID collectOriginId = shipment.getOperationOrigem().getId();
                    if (collectOriginId != null) {
                        double peso = (shipment.getWeight() != null) ? shipment.getWeight() : 0.0;
                        double volume = (shipment.getVolume() != null) ? shipment.getVolume() : 0.0;

                        weightPerCollect.put(collectOriginId, weightPerCollect.getOrDefault(collectOriginId, 0.0) + peso);
                        volumePerCollect.put(collectOriginId, volumePerCollect.getOrDefault(collectOriginId, 0.0) + volume);
                    }
                }
            }
        }

        for (RouteStop stop : stops) {
            Shipment shipment = stop.getShipment();
            if (shipment == null || shipment.getTypeOperation() == null) {
                continue;
            }

            String typeOperation = shipment.getTypeOperation().toString().trim();

            if ("COLETA".equalsIgnoreCase(typeOperation)) {
                UUID idCollect = shipment.getId();
                stop.setWeight(weightPerCollect.getOrDefault(idCollect, 0.0));
                stop.setVolume(volumePerCollect.getOrDefault(idCollect, 0.0));
            } else if ("ENTREGA".equalsIgnoreCase(typeOperation)) {
                stop.setWeight(shipment.getWeight() != null ? shipment.getWeight() : 0.0);
                stop.setVolume(shipment.getVolume() != null ? shipment.getVolume() : 0.0);
            }
        }
    }
}
