package GoLogAPI.controller;

import GoLogAPI.service.routeOptimization.ProcessRouteService;
import GoLogAPI.service.routeOptimization.RouteRequestService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api-route-optimization")
public class RouteOptimizationController {

    private final RouteRequestService routeRequestService;
    private final ProcessRouteService processRouteService;

    public RouteOptimizationController(RouteRequestService routeRequestService, ProcessRouteService processRouteService){
        this.routeRequestService = routeRequestService;
        this.processRouteService = processRouteService;
    }

    @PostMapping
    public String optimizeRoutes(){
        routeRequestService.optimizeRoutes();
        processRouteService.processRoute();
        return routeRequestService.optimizeRoutes();
    }
}
