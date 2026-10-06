package GoLogAPI.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import GoLogAPI.dto.routeStop.RouteStopResponseList;
import GoLogAPI.service.RouteStopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("route-stops")
@Tag(name = "Paradas de Rota")
public class RouteStopController {

    private final RouteStopService routeStopService;

    public RouteStopController(RouteStopService routeStopService){
        this.routeStopService = routeStopService;
    }

    @Operation(summary = "Listar Paradas de Rota", description = "Retorna uma lista de todas as paradas das rotas")
    @GetMapping
    public ResponseEntity<List<RouteStopResponseList>> getAll(){
        List<RouteStopResponseList> routeStopResponses = routeStopService.getAll();
        return ResponseEntity.ok(routeStopResponses);
    }

}
