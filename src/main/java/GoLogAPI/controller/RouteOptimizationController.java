package GoLogAPI.controller;

import GoLogAPI.dto.optimizeRoute.OptimizeRouteRequest;
import GoLogAPI.dto.optimizeRoute.OptimizeRouteResult;
import GoLogAPI.model.Transport;
import GoLogAPI.service.routeOptimization.ProcessRouteService;
import GoLogAPI.service.routeOptimization.RouteRequestService;
import com.google.maps.routeoptimization.v1.OptimizeToursResponse;
import com.google.maps.routeoptimization.v1.SkippedShipment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api-route-optimization")
@Tag(name = "Transportes & Roteirização")
public class RouteOptimizationController {

    private static final Map<String, String> SKIP_REASONS = Map.of(
            "NO_VEHICLE", "nenhum veículo disponível",
            "DEMAND_EXCEEDS_VEHICLE_CAPACITY", "peso/volume acima da capacidade dos veículos",
            "CANNOT_BE_PERFORMED_WITHIN_VEHICLE_DISTANCE_LIMIT", "fora do limite de distância do veículo",
            "CANNOT_BE_PERFORMED_WITHIN_VEHICLE_DURATION_LIMIT", "não cabe na duração máxima da rota",
            "CANNOT_BE_PERFORMED_WITHIN_VEHICLE_TRAVEL_DURATION_LIMIT", "não cabe no tempo de viagem do veículo",
            "CANNOT_BE_PERFORMED_WITHIN_VEHICLE_TIME_WINDOWS", "fora da janela de horário das escalas",
            "VEHICLE_NOT_ALLOWED", "veículo incompatível com o tipo de carga/visita");

    private final RouteRequestService routeRequestService;
    private final ProcessRouteService processRouteService;

    public RouteOptimizationController(RouteRequestService routeRequestService, ProcessRouteService processRouteService) {
        this.routeRequestService = routeRequestService;
        this.processRouteService = processRouteService;
    }

    @Operation(summary = "Otimizar Rotas", description = "Roteiriza as remessas selecionadas nas escalas informadas via Google Route Optimization, "
            + "grava transportes e paradas e devolve o resumo com as remessas não atendidas e o motivo.")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public OptimizeRouteResult optimizeRoutes(@RequestBody OptimizeRouteRequest request) {
        OptimizeToursResponse response = routeRequestService.optimizeRoutes(request);
        List<Transport> transports = processRouteService.processRoute(response, request);

        List<OptimizeRouteResult.SkippedShipment> skipped = new ArrayList<>();
        for (SkippedShipment s : response.getSkippedShipmentsList()) {
            List<String> reasons = s.getReasonsList().stream()
                    .map(r -> SKIP_REASONS.getOrDefault(r.getCode().name(), r.getCode().name()))
                    .distinct()
                    .toList();
            skipped.add(new OptimizeRouteResult.SkippedShipment(idsFromLabel(s.getLabel()), s.getLabel(),
                    reasons.isEmpty() ? List.of("restrições de horário/capacidade") : reasons));
        }

        int planned = response.getRoutesList().stream().mapToInt(r -> r.getVisitsCount()).sum();
        long distance = response.getRoutesList().stream()
                .mapToLong(r -> r.hasMetrics() ? (long) r.getMetrics().getTravelDistanceMeters() : 0L).sum();
        int usedRoutes = (int) response.getRoutesList().stream().filter(r -> r.getVisitsCount() > 0).count();

        return new OptimizeRouteResult(usedRoutes, planned,
                transports.stream().map(Transport::getId).toList(), skipped, distance, response.getMetrics().getTotalCost());
    }

    private static List<UUID> idsFromLabel(String label) {
        List<UUID> ids = new ArrayList<>();
        if (label == null) return ids;
        for (String part : label.split("/")) {
            try { ids.add(UUID.fromString(part)); } catch (IllegalArgumentException ignored) { }
        }
        return ids;
    }
}
