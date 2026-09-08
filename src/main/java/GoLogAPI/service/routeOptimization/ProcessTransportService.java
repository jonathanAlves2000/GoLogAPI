package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.dtoRouteOptimization.response.ApiRouteTransition;
import GoLogAPI.dto.dtoRouteOptimization.response.ApiVehicleRoute;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.*;
import GoLogAPI.model.enums.WorkScheduleStatus;
import GoLogAPI.repository.*;
import GoLogAPI.service.MessageException;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ProcessTransportService {

    private final TransportRepository transportRepository;
    private final EquipamentGroupRepository equipamentGroupRepository;
    private final EquipamentRepository equipamentRepository;
    private final CompanyRepository companyRepository;
    private final WorkScheduleRepository workScheduleRepository;

    public ProcessTransportService(TransportRepository transportRepository, EquipamentGroupRepository equipamentGroupRepository, EquipamentRepository equipamentRepository,
                                   CompanyRepository companyRepository, WorkScheduleRepository workScheduleRepository) {
        this.transportRepository = transportRepository;
        this.equipamentGroupRepository = equipamentGroupRepository;
        this.equipamentRepository = equipamentRepository;
        this.companyRepository = companyRepository;
        this.workScheduleRepository = workScheduleRepository;
    }

    @Transactional
    public Transport processTransport(ApiVehicleRoute vehicleRoute, Double costKmCalculed, Double costHourCalculaed, Double custoTotalCalculed) {
        if(vehicleRoute.visits() == null || vehicleRoute.visits().isEmpty()) {
            return null;
        }

        String vehicleLabel = vehicleRoute.vehicleLabel();

        Integer totalDistance = vehicleRoute.metrics().travelDistanceMeters();

        Integer totalDuration = vehicleRoute.metrics().totalDuration() != null ?
                parseApiRouteDuration(vehicleRoute.metrics().totalDuration()) : 0;

        Integer travelDuration = vehicleRoute.metrics().travelDuration() != null ?
                parseApiRouteDuration(vehicleRoute.metrics().travelDuration()) : 0;

        Integer totalWait = vehicleRoute.metrics().waitDuration() != null ?
                parseApiRouteDuration(vehicleRoute.metrics().waitDuration().toString()) : 0;

        Equipament equipament = equipamentRepository.findByPlate(vehicleLabel)
                .orElseThrow(() -> new RuntimeException("Placa: " + vehicleLabel + " não encontrada"));

        EquipamentGroup equipamentGroup = equipamentGroupRepository.findByEquipament1Id(equipament.getId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipament.getId()));

        Company company = companyRepository.findById(equipament.getCompany().getId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipament.getCompany().getId()));

        Transport transport = transportRepository.findByEquipamentGroup(equipamentGroup)
                .orElse(new Transport());

        Driver driver = null;
        for(WorkSchedule workSchedule : workScheduleRepository.findByEquipamentGroupId(equipamentGroup.getId())) {
            driver = workSchedule.getDriver();
            workSchedule.setStatus(WorkScheduleStatus.EM_OPERACAO);
            workScheduleRepository.save(workSchedule);
        }

        transport.setShipmentQuantity(vehicleRoute.visits().size());
        transport.setCalculedDistance(totalDistance);
        transport.setTravelDuration(travelDuration);
        transport.setTotalTimeCalculed(totalDuration);
        transport.setTimeStoppedCalculed(totalWait);
        transport.setCostKmCalculed(costKmCalculed);
        transport.setCostHourCalculed(costHourCalculaed);
        transport.setTotalCostCalculed(custoTotalCalculed);
        transport.setEquipamentGroup(equipamentGroup);
        transport.setTransporter(company);
        transport.setDriver(driver);

        List<LatLng> totalRoutePoints = new ArrayList<>();

        if(vehicleRoute.transitions() != null) {
            for(ApiRouteTransition transition : vehicleRoute.transitions()) {
                if(transition.routePolyline() != null && transition.routePolyline().points() != null) {
                    List<LatLng> points = OptimizeListLocation.procesingRouteGoogle(transition.routePolyline().points());
                    if(points != null) {
                        totalRoutePoints.addAll(points);
                    }
                }
            }
        }

        if(!totalRoutePoints.isEmpty()) {
            String totalPolylineCode = com.google.maps.internal.PolylineEncoding.encode(totalRoutePoints);
            transport.setRoutePlanned(totalPolylineCode);
        }

        return transportRepository.save(transport);
    }

    private Integer parseApiRouteDuration (String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            return 0;
        }
        String number = durationStr.replace("s", "");

        return Integer.parseInt(number);
    }
}