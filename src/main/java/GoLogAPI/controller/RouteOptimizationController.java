package GoLogAPI.controller;

import GoLogAPI.service.routeOptimization.ProcessRouteService;
import GoLogAPI.service.routeOptimization.RouteRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api-route-optimization")
@Tag(name = "Otimização de Rota")
public class RouteOptimizationController {

    private final RouteRequestService routeRequestService;
    private final ProcessRouteService processRouteService;

    public RouteOptimizationController(RouteRequestService routeRequestService, ProcessRouteService processRouteService){
        this.routeRequestService = routeRequestService;
        this.processRouteService = processRouteService;
    }

    @Operation(summary = "Otimizar Rotas", description = "Realiza o processo de otimização de rotas de transporte com base nas cargas e capacidades")
    @PostMapping
    public String optimizeRoutes(){
        routeRequestService.optimizeRoutes();
        processRouteService.processRoute();
        return routeRequestService.optimizeRoutes();
    }
}
