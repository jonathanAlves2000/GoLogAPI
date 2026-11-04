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

        Equipament equipament = equipamentRepository.findByPlate(vehicleLabel)
                .orElseThrow(() -> new RuntimeException("Placa: " + vehicleLabel + " não encontrada"));

        EquipamentGroup equipamentGroup = equipamentGroupRepository.findByEquipament1Id(equipament.getId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipament.getId()));

        Company company = null;
        // 1. Empresa dona do conjunto de equipamentos (se for transportadora/tenant habilitado)
        if (equipamentGroup.getCompany() != null && !Boolean.TRUE.equals(equipamentGroup.getCompany().getIsCliente())) {
            company = equipamentGroup.getCompany();
        }
        // 2. Empresa dona do cavalo/veículo se não for cliente
        if (company == null && equipament.getCompany() != null) {
            Company eqComp = companyRepository.findById(equipament.getCompany().getId()).orElse(null);
            if (eqComp != null && !Boolean.TRUE.equals(eqComp.getIsCliente())) {
                company = eqComp;
            }
        }
        // 3. Tenant ativo da requisição se for operadora de transporte
        UUID currentTenant = TenantContext.getCurrentTenantId();
        if (company == null && currentTenant != null) {
            Company tenantComp = companyRepository.findById(currentTenant).orElse(null);
            if (tenantComp != null && !Boolean.TRUE.equals(tenantComp.getIsCliente())) {
                company = tenantComp;
            }
        }
        // 4. Empresa do usuário logado se operadora de transporte
        UUID userCompId = TenantContext.getUserCompanyId();
        if (company == null && userCompId != null) {
            Company userComp = companyRepository.findById(userCompId).orElse(null);
            if (userComp != null && !Boolean.TRUE.equals(userComp.getIsCliente())) {
                company = userComp;
            }
        }
        // 5. Empresa Master do sistema (operadora central)
        if (company == null) {
            company = companyRepository.findAll().stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsMaster()))
                    .findFirst()
                    .orElse(null);
        }
        // 6. Qualquer empresa operadora que não seja cliente
        if (company == null) {
            company = companyRepository.findAll().stream()
                    .filter(c -> !Boolean.TRUE.equals(c.getIsCliente()))
                    .findFirst()
                    .orElseGet(() -> companyRepository.findAll().stream().findFirst().orElse(null));
        }

        Transport transport = transportRepository.findByEquipamentGroup(equipamentGroup)
                .orElse(new Transport());

        WorkSchedule workSchedule = workScheduleRepository.findByEquipamentGroupId(equipamentGroup.getId());
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
        Double costHourCalculated = operationDurationHour * driverCostPerHour;
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

        if (!totalRoutePoints.isEmpty()) {
            String totalPolylineCode = com.google.maps.internal.PolylineEncoding.encode(totalRoutePoints);
            transport.setRoutePlanned(totalPolylineCode);
        }

        return transportRepository.save(transport);
    }
}
