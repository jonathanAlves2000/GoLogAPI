package GoLogAPI.controller;

import GoLogAPI.service.routeOptimization.RouteProcessService;
import GoLogAPI.service.routeOptimization.RouteRequestService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api-route-optimization")
public class RouteOptimizationController {

    private final RouteRequestService routeRequestService;
    private final RouteProcessService routeProcessService;

    public RouteOptimizationController(RouteRequestService routeRequestService, RouteProcessService routeProcessService){
        this.routeRequestService = routeRequestService;
        this.routeProcessService = routeProcessService;
    }

    @PostMapping
    public String optimizeRoutes(){
        routeRequestService.optimizeRoutes();
        routeProcessService.saveRoute();
        return routeRequestService.optimizeRoutes();
    }
}
