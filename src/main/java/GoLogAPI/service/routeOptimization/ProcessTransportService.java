package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.dtoRouteOptimization.response.ApiRouteTransition;
import GoLogAPI.dto.dtoRouteOptimization.response.ApiVehicleRoute;
import GoLogAPI.dto.optimizeRoute.OptimizeRouteRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.*;
import GoLogAPI.model.enums.RoutePriority;
import GoLogAPI.model.enums.WorkScheduleStatus;
import GoLogAPI.repository.*;
import GoLogAPI.service.MessageException;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProcessTransportService {

    private final TransportRepository transportRepository;
    private final EquipamentGroupRepository equipamentGroupRepository;
    private final EquipamentRepository equipamentRepository;
    private final CompanyRepository companyRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final OptimizationConfigResolver configResolver;

    public ProcessTransportService(TransportRepository transportRepository, EquipamentGroupRepository equipamentGroupRepository, EquipamentRepository equipamentRepository,
                                   CompanyRepository companyRepository, WorkScheduleRepository workScheduleRepository,
                                   OptimizationConfigResolver configResolver) {
        this.transportRepository = transportRepository;
        this.equipamentGroupRepository = equipamentGroupRepository;
        this.equipamentRepository = equipamentRepository;
        this.companyRepository = companyRepository;
        this.workScheduleRepository = workScheduleRepository;
        this.configResolver = configResolver;
    }

    @Transactional
    public Transport processTransport(ApiVehicleRoute vehicleRoute, RoutePriority routePriority) {
        return processTransport(vehicleRoute, new OptimizeRouteRequest(null, null, routePriority));
    }

    @Transactional
    public Transport processTransport(ApiVehicleRoute vehicleRoute, OptimizeRouteRequest optimizeRouteRequest) {

        if (vehicleRoute.visits() == null || vehicleRoute.visits().isEmpty()) {
            return null;
        }

        String vehicleLabel = vehicleRoute.vehicleLabel();

        Equipament equipament = equipamentRepository.findByPlate(vehicleLabel)
                .orElseThrow(() -> new RuntimeException("Placa: " + vehicleLabel + " não encontrada"));

        EquipamentGroup equipamentGroup = equipamentGroupRepository.findByEquipament1Id(equipament.getId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipament.getId()));

        Company company = companyRepository.findById(equipament.getCompany().getId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipament.getCompany().getId()));

        Transport transport = transportRepository.findByEquipamentGroup(equipamentGroup)
                .orElse(new Transport());

        WorkSchedule workSchedule = workScheduleRepository.findByEquipamentGroupId(equipamentGroup.getId());
        Driver driver = null;
        driver = workSchedule.getDriver();
        workSchedule.setStatus(WorkScheduleStatus.EM_OPERACAO);
        workScheduleRepository.save(workSchedule);

        Integer totalDistance = vehicleRoute.metrics().travelDistanceMeters();

        Integer visitDuration = vehicleRoute.metrics().visitDuration() != null ?
                parseApiRouteDuration(vehicleRoute.metrics().visitDuration()) : 0;

        Integer travelDuration = vehicleRoute.metrics().travelDuration() != null ?
                parseApiRouteDuration(vehicleRoute.metrics().travelDuration()) : 0;

        Integer totalDuration = vehicleRoute.metrics().totalDuration() != null ?
                parseApiRouteDuration(vehicleRoute.metrics().totalDuration()) : 0;

        Integer operationalDuration = visitDuration + travelDuration;
        Integer operarionDurationHour = operationalDuration / 3600;

        Integer totalWait = vehicleRoute.metrics().waitDuration() != null ?
                parseApiRouteDuration(vehicleRoute.metrics().waitDuration().toString()) : 0;

        OptimizationConfigResolver.ResolvedOptimizationSettings settings = configResolver.resolveSettings(optimizeRouteRequest, company);
        double kmMultiplier = settings.kmCostMultiplier() > 0 ? settings.kmCostMultiplier() : 1.0;

        Map<String, Double> routeCosts = vehicleRoute.routeCosts();
        Double costKmMultiplied = routeCosts.get("model.vehicles.cost_per_kilometer");
        Double costKmCalculated = costKmMultiplied != null ? costKmMultiplied / kmMultiplier : 0.0;
        Double costHourCalculated = operarionDurationHour * driver.getCostPerHour();
        Double custoTotalCalculated = costKmCalculated + costHourCalculated + settings.fixedCostPerVehicle();

        transport.setShipmentQuantity(vehicleRoute.visits().size());
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

        if (vehicleRoute.transitions() != null) {
            for (ApiRouteTransition transition : vehicleRoute.transitions()) {
                if (transition.routePolyline() != null && transition.routePolyline().points() != null) {
                    List<LatLng> points = OptimizeListLocation.procesingRouteGoogle(transition.routePolyline().points());
                    if (points != null) {
                        totalRoutePoints.addAll(points);
                    }
                }
            }
        }

        if (!totalRoutePoints.isEmpty()) {
            String totalPolylineCode = com.google.maps.internal.PolylineEncoding.encode(totalRoutePoints);
            transport.setRoutePlanned(totalPolylineCode);
        }

        return transportRepository.save(transport);
    }

    private Integer parseApiRouteDuration(String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            return 0;
        }
        String number = durationStr.replace("s", "");

        return Integer.parseInt(number);
    }
}
