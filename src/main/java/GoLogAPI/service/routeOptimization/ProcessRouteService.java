package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.optimizeRoute.OptimizeRouteRequest;
import GoLogAPI.model.enums.RoutePriority;
import com.google.maps.routeoptimization.v1.OptimizeToursResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProcessRouteService {

    private final ProcessRouteStopService processShipment;

    public ProcessRouteService(ProcessRouteStopService processShipment) {
        this.processShipment = processShipment;
    }

    public java.util.List<GoLogAPI.model.Transport> processRoute(OptimizeToursResponse response, RoutePriority routePriority) {
        if (response != null && response.getRoutesCount() > 0) {
            return processShipment.processShipment(response.getRoutesList(), routePriority);
        }
        return java.util.List.of();
    }

    public java.util.List<GoLogAPI.model.Transport> processRoute(OptimizeToursResponse response, OptimizeRouteRequest optimizeRouteRequest) {
        if (response != null && response.getRoutesCount() > 0) {
            return processShipment.processShipment(response.getRoutesList(), optimizeRouteRequest);
        }
        return java.util.List.of();
    }
}
