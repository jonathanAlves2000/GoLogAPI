package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.dtoRouteOptimization.response.ApiRouteOptimizationResponse;
import GoLogAPI.dto.dtoRouteOptimization.response.ApiRouteStop;
import GoLogAPI.dto.dtoRouteOptimization.response.ApiRouteTransition;
import GoLogAPI.dto.dtoRouteOptimization.response.ApiVehicleRoute;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.Shipment;
import GoLogAPI.repository.ShipmentRepository;
import GoLogAPI.repository.TransportRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import GoLogAPI.service.MessageException;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RouteProcessService {

    private final RouteRequestService routeRequestService;
    private final ObjectMapper objectMapper;
    private final ShipmentRepository shipmentRepository;
    private final TransportRepository transportRepository;

    public RouteProcessService(RouteRequestService routeRequestService, ObjectMapper objectMapper,
                               ShipmentRepository shipmentRepository, TransportRepository transportRepository)
    {
        this.routeRequestService = routeRequestService;
        this.objectMapper = objectMapper;
        this.shipmentRepository = shipmentRepository;
        this.transportRepository = transportRepository;
    }

    public void saveRoute() {
        String routeResponseString = routeRequestService.optimizeRoutes();

        try {
            ApiRouteOptimizationResponse routeResponseObject = objectMapper.readValue(
                    routeResponseString,
                    ApiRouteOptimizationResponse.class
            );

            if (routeResponseObject != null && routeResponseObject.routes() != null) {
                processRouteResponse(routeResponseObject.routes());
            }

        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new RuntimeException("Falha ao deserializar o JSON de rotas do Google");
        }

    }

    @Transactional
    public void processRouteResponse(List<ApiVehicleRoute> routes){
        for(ApiVehicleRoute vehicleRoute : routes) {

            if (vehicleRoute.transitions() == null || vehicleRoute.transitions().isEmpty())
                continue;

            Double costTotalRoute = vehicleRoute.routeTotalCost();
            Double distanceTotal = (vehicleRoute.metrics() != null) ? vehicleRoute.metrics().travelDistanceMeters() : 0.0;
            List<ApiRouteStop> visits = vehicleRoute.visits();
            List<ApiRouteTransition> transitions = vehicleRoute.transitions();

            if (visits != null) {
                for (int i = 0; i < visits.size(); i++) {
                    ApiRouteStop routeStop = visits.get(i);

                    String labelId = routeStop.shipmentLabel();

                    if(labelId != null && labelId.contains("/")){
                        String[] ids = labelId.split("/");
                        UUID collectId = UUID.fromString(ids[0]);
                        UUID deliveryId = UUID.fromString(ids[1]);
                        UUID shipmentId = routeStop.isPickup() ? collectId : deliveryId;

                        int sequenceOrder = i + 1;

                        Shipment shipment = shipmentRepository.findById(shipmentId)
                                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentId));

                        shipment.setShippingSequence(sequenceOrder);
                        shipmentRepository.save(shipment);

                    }
                }
            }

            if (transitions != null) {
                for (int i = 0; i < transitions.size(); i++) {
                    ApiRouteTransition routeTransition = transitions.get(i);

                    if (routeTransition.routePolyline() == null || routeTransition.routePolyline().points() == null)
                        continue;

                    if (i >= visits.size())
                        continue;

                    ApiRouteStop stopDart = visits.get(i);
                    String labelId = stopDart.shipmentLabel().toString();

                    String[] ids = labelId.split("/");
                    UUID collectId = UUID.fromString(ids[0]);
                    UUID deliveryId = UUID.fromString(ids[1]);
                    UUID shipmentId = stopDart.isPickup() ? collectId : deliveryId;

                    Double duration = parseApiRouteDuration(routeTransition.travelDuration().toString());

                    Double waitDuration = routeTransition.waitDuration() != null ?
                            parseApiRouteDuration(routeTransition.waitDuration().toString()) : 0.0;

                    Double travelDistanceMeters = routeTransition.travelDistanceMeters() != null ?
                            routeTransition.travelDistanceMeters().doubleValue() : 0;

                    List<LatLng> optimizedPoints = OptimizeListLocation.procesingRouteGoogle(routeTransition.routePolyline().points());
                    String polylineCode = com.google.maps.internal.PolylineEncoding.encode(optimizedPoints);

                    Double costRoute = 0.0;

                    if(distanceTotal > 0){
                        costRoute = (travelDistanceMeters / distanceTotal) * costTotalRoute;
                    }

                    Shipment shipment = shipmentRepository.findById(shipmentId)
                            .orElseThrow(() ->  new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentId));

                    shipment.setRoutePlanned(polylineCode);
                    shipment.setCalculatedCost(costRoute);
                    shipment.setCalculatedDistance(travelDistanceMeters);
                    shipment.setCalculatedDuration(duration);
                    shipment.setCalculatedWait(waitDuration);
                    shipmentRepository.save(shipment);

                }

            }
        }

    }

    private Double parseApiRouteDuration (String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            return 0.0;
        }
        String number = durationStr.replace("s", "");

        return Double.parseDouble(number);
    }

}
