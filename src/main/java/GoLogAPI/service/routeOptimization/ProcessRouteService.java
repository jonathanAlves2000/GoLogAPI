package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.dtoRouteOptimization.response.ApiRouteOptimizationResponse;
import GoLogAPI.model.enums.RoutePriority;
import GoLogAPI.repository.ShipmentRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProcessRouteService {

    private final RouteRequestService routeRequestService;
    private final ObjectMapper objectMapper;
    private final ProcessRouteStopService processShipment;

    public ProcessRouteService(RouteRequestService routeRequestService, ObjectMapper objectMapper,
                               ShipmentRepository shipmentRepository, ProcessRouteStopService processShipment)
    {
        this.routeRequestService = routeRequestService;
        this.objectMapper = objectMapper;
        this.processShipment = processShipment;
    }

    public void processRoute(String routeResponseString, RoutePriority optimizeRouteRequest) {

        try {
            ApiRouteOptimizationResponse routeResponseObject = objectMapper.readValue(
                    routeResponseString,
                    ApiRouteOptimizationResponse.class
            );

            if(routeResponseObject != null && routeResponseObject.routes() != null) {
                processShipment.processShipment(routeResponseObject.routes(), optimizeRouteRequest);
            }

        } catch(JsonProcessingException e) {
            throw new RuntimeException(e + " Falha ao deserializar o JSON de rotas do Google");
        }
    }
}
