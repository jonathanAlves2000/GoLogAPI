package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.dtoRouteOptimization.response.ApiRouteOptimizationResponse;
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
    private final ProcessShipmentServcie processShipment;
    private final ProcessTransportService processTransport;

    public ProcessRouteService(RouteRequestService routeRequestService, ObjectMapper objectMapper,
                               ShipmentRepository shipmentRepository, ProcessShipmentServcie processShipment,
                               ProcessTransportService processTransport)
    {
        this.routeRequestService = routeRequestService;
        this.objectMapper = objectMapper;
        this.processShipment = processShipment;
        this.processTransport = processTransport;
    }

    public void processRoute() {
        String routeResponseString = routeRequestService.optimizeRoutes();
        try {
            ApiRouteOptimizationResponse routeResponseObject = objectMapper.readValue(
                    routeResponseString,
                    ApiRouteOptimizationResponse.class
            );

            if(routeResponseObject != null && routeResponseObject.routes() != null) {
                processShipment.processShipment(routeResponseObject.routes());
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e + " Falha ao deserializar o JSON de rotas do Google");
        }
    }
}
