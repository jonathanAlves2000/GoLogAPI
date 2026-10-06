package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.optimizeRoute.OptimizeRouteRequest;
import GoLogAPI.model.enums.RoutePriority;
import com.google.cloud.optimization.v1.OptimizeToursResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProcessRouteService {

    private final ProcessRouteStopService processShipment;

    public ProcessRouteService(ProcessRouteStopService processShipment) {
        this.processShipment = processShipment;
    }

    public void processRoute(OptimizeToursResponse response, RoutePriority routePriority) {
        if (response != null && response.getRoutesCount() > 0) {
            processShipment.processShipment(response.getRoutesList(), routePriority);
        }
    }

    public void processRoute(OptimizeToursResponse response, OptimizeRouteRequest optimizeRouteRequest) {
        if (response != null && response.getRoutesCount() > 0) {
            processShipment.processShipment(response.getRoutesList(), optimizeRouteRequest);
        }
    }
}
