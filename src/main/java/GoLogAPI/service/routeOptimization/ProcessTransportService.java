package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.dtoRouteOptimization.response.ApiRouteStop;
import GoLogAPI.dto.dtoRouteOptimization.response.ApiRouteTransition;
import GoLogAPI.dto.dtoRouteOptimization.response.ApiVehicleRoute;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.*;
import GoLogAPI.repository.*;
import GoLogAPI.service.MessageException;
import com.google.maps.model.LatLng;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ProcessTransportService {

    private final TransportRepository transportRepository;
    private final EquipamentGroupRepository equipamentGroupRepository;
    private final EquipamentRepository equipamentRepository;
    private final CompanyRepository companyRepository;

    public ProcessTransportService(TransportRepository transportRepository, EquipamentGroupRepository equipamentGroupRepository, EquipamentRepository equipamentRepository,
                                   CompanyRepository companyRepository) {
        this.transportRepository = transportRepository;
        this.equipamentGroupRepository = equipamentGroupRepository;
        this.equipamentRepository = equipamentRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public Transport processTransport(ApiVehicleRoute vehicleRoute, Double totalCost) {
        if (vehicleRoute.visits() == null || vehicleRoute.visits().isEmpty()) {
            return null;
        }

        String vehicleLabel = vehicleRoute.vehicleLabel();

        Integer totalDistance = vehicleRoute.metrics().travelDistanceMeters();
        Integer totalDuration = parseApiRouteDuration(vehicleRoute.metrics().travelDuration());
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

        transport.setShipmentQuantity(vehicleRoute.visits().size());
        transport.setCalculedDistance(totalDistance);
        transport.setTotalTimeCalculed(totalDuration);
        transport.setTimeStoppedCalculed(totalWait);
        transport.setTotalCostCalculed(totalCost);
        transport.setEquipamentGroup(equipamentGroup);
        transport.setTransporter(company);

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