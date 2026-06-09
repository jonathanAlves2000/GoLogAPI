package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.dtoRouteOptimization.response.ApiRouteStop;
import GoLogAPI.dto.dtoRouteOptimization.response.ApiRouteTransition;
import GoLogAPI.dto.dtoRouteOptimization.response.ApiVehicleRoute;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.repository.RouteStopRepository;
import GoLogAPI.repository.ShipmentRepository;
import GoLogAPI.service.MessageException;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProcessShipmentServcie {

    private final ShipmentRepository shipmentRepository;
    private final ProcessTransportService processTransportService;
    private final RouteStopRepository routeStopRepository;

    public ProcessShipmentServcie(ShipmentRepository shipmentRepository, ProcessTransportService processTransportService,
                                  RouteStopRepository routeStopRepository)
    {
        this.shipmentRepository = shipmentRepository;
        this.processTransportService = processTransportService;
        this.routeStopRepository = routeStopRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    public void processShipment(List<ApiVehicleRoute> routes) {
        for (ApiVehicleRoute vehicleRoute : routes) {

            if (vehicleRoute.transitions() == null || vehicleRoute.transitions().isEmpty())
                continue;

            Double costTotalRoute = vehicleRoute.routeTotalCost() != null ? vehicleRoute.routeTotalCost() : 0.0;

            Transport transport = processTransportService.processTransport(vehicleRoute, costTotalRoute);

            if (transport == null)
                continue;

            routeStopRepository.deleteByTransportId(transport.getId());

            routeStopRepository.flush();

            Double distanceTotal = (vehicleRoute.metrics() != null) ? vehicleRoute.metrics().travelDistanceMeters() : 0.0;
            List<ApiRouteStop> visits = vehicleRoute.visits();
            List<ApiRouteTransition> transitions = vehicleRoute.transitions();
            List<RouteStop> newStops = new ArrayList<>();

            if (transitions != null && visits != null) {
                for (int i = 0; i < transitions.size(); i++) {
                    ApiRouteTransition routeTransition = transitions.get(i);

                    if (routeTransition.routePolyline() == null || routeTransition.routePolyline().points() == null)
                        continue;

                    Integer duration = parseApiRouteDuration(routeTransition.travelDuration().toString());
                    Integer waitDuration = routeTransition.waitDuration() != null ?
                            parseApiRouteDuration(routeTransition.waitDuration().toString()) : 0;
                    Integer travelDistanceMeters = routeTransition.travelDistanceMeters() != null ?
                            routeTransition.travelDistanceMeters().intValue() : 0;

                    List<LatLng> optimizedPoints = OptimizeListLocation.procesingRouteGoogle(routeTransition.routePolyline().points());
                    String polylineCode = com.google.maps.internal.PolylineEncoding.encode(optimizedPoints);

                    Double costRoute = 0.0;
                    if (distanceTotal > 0) {
                        costRoute = (travelDistanceMeters / distanceTotal) * costTotalRoute;
                    }

                    int sequenceOrder = i + 1;
                    Shipment shipment = null;

                    if(i < visits.size()) {
                        ApiRouteStop currentStop = visits.get(i);
                        String[] ids = currentStop.shipmentLabel().split("/");
                        UUID collectId = UUID.fromString(ids[0]);
                        UUID deliveryId = UUID.fromString(ids[1]);

                        UUID targetId = currentStop.isPickup() ? collectId : deliveryId;

                        shipment = shipmentRepository.findById(targetId)
                                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, targetId));
                    }

                    if (shipment == null) {
                        continue;
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
                    shipment.setStatus("Aguardando Inicio");

                    RouteStop routeStop = routeStopRepository.save(stop);
                    shipmentRepository.save(shipment);
                    newStops.add(routeStop);
                }
                this.updateWeightAndVolume(newStops);
            }
        }
    }

    private void updateWeightAndVolume(List<RouteStop> stops) {

        if(stops == null || stops.isEmpty()) {
            return;
        }

        // 1. Calcula os totais das ENTREGAS (Forçando conversão limpa para String)
        double weightTotalShipment = 0.0;
        double volumeTotalShipment = 0.0;

        for (RouteStop stop : stops) {
            if(stop.getShipment() != null && stop.getShipment().getTypeOperation() != null) {
                // Pega o nome do tipo (funciona tanto para String quanto para Enum)
                String typeStr = stop.getShipment().getTypeOperation().toString().trim();

                if("ENTREGA".equalsIgnoreCase(typeStr)) {
                    weightTotalShipment += (stop.getShipment().getWeight() != null) ? stop.getShipment().getWeight() : 0.0;
                    volumeTotalShipment += (stop.getShipment().getVolume() != null) ? stop.getShipment().getVolume() : 0.0;
                }
            }
        }

        // 2. Aplica os valores calculados diretamente nas entidades RouteStop da lista
        for(RouteStop stop : stops) {
            if(stop.getShipment() == null || stop.getShipment().getTypeOperation() == null) {
                continue;
            }

            String typeOperation = stop.getShipment().getTypeOperation().toString().trim();

            if("COLETA".equalsIgnoreCase(typeOperation)) {
                // Coleta recebe o acumulado das entregas
                stop.setWeight(weightTotalShipment);
                stop.setVolume(volumeTotalShipment);
            } else if ("ENTREGA".equalsIgnoreCase(typeOperation)) {
                // Entrega espelha o valor do próprio shipment
                stop.setWeight(stop.getShipment().getWeight() != null ? stop.getShipment().getWeight() : 0.0);
                stop.setVolume(stop.getShipment().getVolume() != null ? stop.getShipment().getVolume() : 0.0);
            }

            // Salva a entidade de rota atualizada com os novos valores
            routeStopRepository.save(stop);
        }
    }

    private Integer parseApiRouteDuration (String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            return 0;
        }
        String number = durationStr.replace("s", "");

        return Integer.parseInt(number);
    }
}
