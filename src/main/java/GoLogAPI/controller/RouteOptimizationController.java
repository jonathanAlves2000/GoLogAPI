package GoLogAPI.controller;

import GoLogAPI.dto.optimizeRoute.OptimizeRouteRequest;
import GoLogAPI.service.routeOptimization.ProcessRouteService;
import GoLogAPI.service.routeOptimization.RouteRequestService;
import com.google.cloud.optimization.v1.OptimizeToursResponse;
import com.google.protobuf.util.JsonFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api-route-optimization")
@Tag(name = "Transportes & Roteirização")
public class RouteOptimizationController {

    private final RouteRequestService routeRequestService;
    private final ProcessRouteService processRouteService;

    public RouteOptimizationController(RouteRequestService routeRequestService, ProcessRouteService processRouteService) {
        this.routeRequestService = routeRequestService;
        this.processRouteService = processRouteService;
    }

    @Operation(summary = "Otimizar Rotas", description = "Realiza o processo de otimização de rotas de transporte com base nas cargas e capacidades via gRPC")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String optimizeRoutes(@RequestBody OptimizeRouteRequest optimizeRouteRequest) {
        OptimizeToursResponse response = routeRequestService.optimizeRoutes(optimizeRouteRequest);
        processRouteService.processRoute(response, optimizeRouteRequest);
        try {
            return JsonFormat.printer().print(response);
        } catch (Exception e) {
            return "{\"routesCount\": " + response.getRoutesCount() + "}";
        }
    }
}
