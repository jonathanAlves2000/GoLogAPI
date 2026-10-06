package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.optimizeRoute.OptimizeRouteRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.*;
import GoLogAPI.model.enums.RoutePriority;
import GoLogAPI.model.enums.WorkScheduleStatus;
import GoLogAPI.repository.*;
import GoLogAPI.service.MessageException;
import com.google.cloud.optimization.v1.ShipmentRoute;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        Company company = companyRepository.findById(equipament.getCompany().getId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipament.getCompany().getId()));

        Transport transport = transportRepository.findByEquipamentGroup(equipamentGroup)
                .orElse(new Transport());

        WorkSchedule workSchedule = workScheduleRepository.findByEquipamentGroupId(equipamentGroup.getId());
        Driver driver = workSchedule.getDriver();
        workSchedule.setStatus(WorkScheduleStatus.EM_OPERACAO);
        workScheduleRepository.save(workSchedule);

        var metrics = vehicleRoute.getMetrics();
        int totalDistance = (int) metrics.getTravelDistanceMeters();
        int visitDuration = (int) metrics.getVisitDuration().getSeconds();
        int travelDuration = (int) metrics.getTravelDuration().getSeconds();
        int operationalDuration = visitDuration + travelDuration;
        int operationDurationHour = operationalDuration / 3600;
        int totalWait = (int) metrics.getWaitDuration().getSeconds();

        OptimizationConfigResolver.ResolvedOptimizationSettings settings = configResolver.resolveSettings(optimizeRouteRequest, company);
        double kmMultiplier = settings.kmCostMultiplier() > 0 ? settings.kmCostMultiplier() : 1.0;

        Map<String, Double> routeCosts = metrics.getCostsMap();
        Double costKmMultiplied = routeCosts.get("model.vehicles.cost_per_kilometer");
        Double costKmCalculated = costKmMultiplied != null ? costKmMultiplied / kmMultiplier : 0.0;
        Double costHourCalculated = operationDurationHour * driver.getCostPerHour();
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
