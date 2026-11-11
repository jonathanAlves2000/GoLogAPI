package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.optimizeRoute.OptimizeRouteRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.*;
import GoLogAPI.model.enums.RoutePriority;
import GoLogAPI.model.enums.WorkScheduleStatus;
import GoLogAPI.repository.*;
import GoLogAPI.service.MessageException;
import com.google.maps.routeoptimization.v1.ShipmentRoute;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import GoLogAPI.infra.tenant.TenantContext;

@Service
@Transactional(readOnly = true)
public class ProcessTransportService {

    private final TransportRepository transportRepository;
    private final EquipamentGroupRepository equipamentGroupRepository;
    private final EquipamentRepository equipamentRepository;
    private final CompanyRepository companyRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final OptimizationConfigResolver configResolver;

    public ProcessTransportService(
            TransportRepository transportRepository,
            EquipamentGroupRepository equipamentGroupRepository,
            EquipamentRepository equipamentRepository,
            CompanyRepository companyRepository,
            WorkScheduleRepository workScheduleRepository,
            OptimizationConfigResolver configResolver) {
        this.transportRepository = transportRepository;
        this.equipamentGroupRepository = equipamentGroupRepository;
        this.equipamentRepository = equipamentRepository;
        this.companyRepository = companyRepository;
        this.workScheduleRepository = workScheduleRepository;
        this.configResolver = configResolver;
    }

    @Transactional
    public Transport processTransport(ShipmentRoute vehicleRoute, RoutePriority routePriority) {
        return processTransport(vehicleRoute, new OptimizeRouteRequest(null, null, routePriority));
    }

    @Transactional
    public Transport processTransport(ShipmentRoute vehicleRoute, OptimizeRouteRequest optimizeRouteRequest) {

        if (vehicleRoute.getVisitsList().isEmpty()) {
            return null;
        }

        String vehicleLabel = vehicleRoute.getVehicleLabel();
        Equipament equipament = null;
        EquipamentGroup equipamentGroup = null;
        WorkSchedule workSchedule = null;

        // Suporte a formato composto único: placa/equipamentGroupId/workScheduleId
        if (vehicleLabel != null && vehicleLabel.contains("/")) {
            String[] parts = vehicleLabel.split("/");
            if (parts.length >= 2) {
                try {
                    UUID eqGroupId = UUID.fromString(parts[1]);
                    equipamentGroup = equipamentGroupRepository.findById(eqGroupId).orElse(null);
                } catch (Exception ignored) {}
            }
            if (parts.length >= 3) {
                try {
                    UUID wsId = UUID.fromString(parts[2]);
                    workSchedule = workScheduleRepository.findById(wsId).orElse(null);
                } catch (Exception ignored) {}
            }
            if (equipamentGroup != null && equipamentGroup.getEquipament1() != null) {
                equipament = equipamentGroup.getEquipament1();
            }
        }

        // Fallback para placa simples (compatibilidade com chamadas diretas / legado)
        if (equipament == null) {
            String plate = vehicleLabel != null && vehicleLabel.contains("/") ? vehicleLabel.split("/")[0] : vehicleLabel;
            UUID reqTenant = TenantContext.getCurrentTenantId();
            if (reqTenant != null) {
                equipament = equipamentRepository.findFirstByPlateAndCompanyIdOrderByCreatedAtDesc(plate, reqTenant)
                        .orElse(null);
            }
            if (equipament == null) {
                equipament = equipamentRepository.findFirstByPlateOrderByCreatedAtDesc(plate)
                        .orElseThrow(() -> new RuntimeException("Placa: " + vehicleLabel + " não encontrada"));
            }
        }

        final UUID equipamentId = equipament.getId();
        if (equipamentGroup == null) {
            equipamentGroup = equipamentGroupRepository.findFirstByEquipament1IdOrderByCreatedAtDesc(equipamentId)
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipamentId));
        }

        // O transporte pertence ao tenant dono do conjunto (validado em RouteRequestService)
        Company company = equipamentGroup.getCompany();
        if (company == null && equipament.getCompany() != null) {
            company = equipament.getCompany();
        }
        if (company == null && TenantContext.getCurrentTenantId() != null) {
            company = companyRepository.findById(TenantContext.getCurrentTenantId()).orElse(null);
        }

        // Cada roteirização gera um transporte novo. Reaproveitar o último transporte do conjunto apagava
        // as paradas de planejamentos anteriores ainda não executados.
        Transport transport = new Transport();

        if (workSchedule == null) {
            workSchedule = workScheduleRepository.findFirstByEquipamentGroupIdOrderByCreatedAtDesc(equipamentGroup.getId())
                    .orElse(null);
        }
        Driver driver = workSchedule != null ? workSchedule.getDriver() : null;
        if (workSchedule != null) {
            workSchedule.setStatus(WorkScheduleStatus.EM_OPERACAO);
            workScheduleRepository.save(workSchedule);
        }

        var metrics = vehicleRoute.getMetrics();
        int totalDistance = (int) metrics.getTravelDistanceMeters();
        int visitDuration = (int) metrics.getVisitDuration().getSeconds();
        int travelDuration = (int) metrics.getTravelDuration().getSeconds();
        int operationalDuration = visitDuration + travelDuration;
        int operationDurationHour = operationalDuration / 3600;
        int totalWait = (int) metrics.getWaitDuration().getSeconds();

        OptimizationConfigResolver.ResolvedOptimizationSettings settings = configResolver.resolveSettings(optimizeRouteRequest, company);
        double kmMultiplier = settings.kmCostMultiplier() > 0 ? settings.kmCostMultiplier() : 1.0;

        Map<String, Double> routeCosts = vehicleRoute.getRouteCostsMap();
        Double costKmMultiplied = routeCosts.get("model.vehicles.cost_per_kilometer");
        Double costKmCalculated = costKmMultiplied != null ? costKmMultiplied / kmMultiplier : 0.0;
        Double driverCostPerHour = (driver != null && driver.getCostPerHour() != null) ? driver.getCostPerHour() : 25.0;
        // Horas fracionadas: a divisão inteira zerava o custo de rotas com menos de 1h
        Double costHourCalculated = (operationalDuration / 3600.0) * driverCostPerHour;
        Double custoTotalCalculated = costKmCalculated + costHourCalculated + settings.fixedCostPerVehicle();

        transport.setShipmentQuantity(vehicleRoute.getVisitsCount());
        transport.setCalculedDistance(totalDistance);
        transport.setTravelDuration(travelDuration);
        transport.setTotalTimeCalculed(operationalDuration);
        transport.setTimeStoppedCalculed(totalWait);
        transport.setCostKmCalculed(costKmCalculated);
        transport.setCostHourCalculed(costHourCalculated);
        transport.setTotalCostCalculed(custoTotalCalculated);
        transport.setEquipamentGroup(equipamentGroup);
        transport.setTransporter(company);
        transport.setDriver(driver);

        List<LatLng> totalRoutePoints = new ArrayList<>();
        for (ShipmentRoute.Transition transition : vehicleRoute.getTransitionsList()) {
            if (transition.hasRoutePolyline() && !transition.getRoutePolyline().getPoints().isEmpty()) {
                List<LatLng> points = OptimizeListLocation.procesingRouteGoogle(transition.getRoutePolyline().getPoints());
                if (points != null) {
                    totalRoutePoints.addAll(points);
                }
            }
        }

        // route_planned é NOT NULL: sem polilinha (ex.: todas as paradas no mesmo ponto) grava vazio
        transport.setRoutePlanned(totalRoutePoints.isEmpty() ? ""
                : com.google.maps.internal.PolylineEncoding.encode(totalRoutePoints));

        return transportRepository.save(transport);
    }
}
