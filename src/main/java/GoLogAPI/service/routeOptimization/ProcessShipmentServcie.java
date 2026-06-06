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
import GoLogAPI.repository.TransportRepository;
import GoLogAPI.service.MessageException;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
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

    @Transactional
    public void processShipment(List<ApiVehicleRoute> routes) {
        for (ApiVehicleRoute vehicleRoute : routes) {

            if (vehicleRoute.transitions() == null || vehicleRoute.transitions().isEmpty())
                continue;

            Double costTotalRoute = vehicleRoute.routeTotalCost() != null ? vehicleRoute.routeTotalCost() : 0.0;

            Transport transport = processTransportService.processTransport(vehicleRoute, costTotalRoute);

            if (transport == null)
                continue;

            routeStopRepository.deleteByTransportId(transport.getId());

            Double distanceTotal = (vehicleRoute.metrics() != null) ? vehicleRoute.metrics().travelDistanceMeters() : 0.0;
            List<ApiRouteStop> visits = vehicleRoute.visits();
            List<ApiRouteTransition> transitions = vehicleRoute.transitions();

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

                    // CORREÇÃO AQUI: Relação direta 1 para 1 entre a transição 'i' e a visita 'i'
                    if(i < visits.size()) {
                        ApiRouteStop currentStop = visits.get(i);
                        String[] ids = currentStop.shipmentLabel().split("/");
                        UUID collectId = UUID.fromString(ids[0]);
                        UUID deliveryId = UUID.fromString(ids[1]);

                        // Identifica se o destino da perna é o ponto de coleta ou de entrega
                        UUID targetId = currentStop.isPickup() ? collectId : deliveryId;

                        shipment = shipmentRepository.findById(targetId)
                                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, targetId));
                    }

                    // Se i for igual ao tamanho de visits, esta é a transição final de volta à garagem.
                    // Como não há documento (Shipment) atrelado à garagem, ignoramos o salvamento dela.
                    if (shipment == null) {
                        continue;
                    }

                    // Instancia e salva a Parada Física da Viagem de forma isolada
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

                    routeStopRepository.save(stop);
                    shipmentRepository.save(shipment);
                }
            }
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
