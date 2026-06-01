package GoLogAPI.controller;

import GoLogAPI.infra.client.RouteOptimizationClient;
import GoLogAPI.service.RouteOptimizationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api-route-optimization")
public class RouteOptimizationController {

    private final RouteOptimizationService routeOptimizationService;

    public RouteOptimizationController(RouteOptimizationService routeOptimizationService){
        this.routeOptimizationService = routeOptimizationService;
    }

    @PostMapping
    public String optimizeRoutes(){
        routeOptimizationService.optimizeRoutes();

        return routeOptimizationService.optimizeRoutes();
    }
}
