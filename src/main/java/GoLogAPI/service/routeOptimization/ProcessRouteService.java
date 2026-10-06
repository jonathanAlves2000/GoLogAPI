package GoLogAPI.service.routeOptimization;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import GoLogAPI.dto.dtoRouteOptimization.response.ApiRouteOptimizationResponse;
import GoLogAPI.model.enums.RoutePriority;

@Service
@Transactional(readOnly = true)
public class ProcessRouteService {

    private final ObjectMapper objectMapper;
    private final ProcessRouteStopService processShipment;

    public ProcessRouteService(ObjectMapper objectMapper, ProcessRouteStopService processShipment)
    {
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
