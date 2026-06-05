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
    private final ShipmentRepository shipmentRepository;
    private final EquipamentGroupRepository equipamentGroupRepository;
    private final EquipamentRepository equipamentRepository;
    private final CompanyRepository companyRepository;

    public ProcessTransportService(TransportRepository transportRepository, ShipmentRepository shipmentRepository,
                                   EquipamentGroupRepository equipamentGroupRepository, EquipamentRepository equipamentRepository,
                                   CompanyRepository companyRepository) {
        this.transportRepository = transportRepository;
        this.shipmentRepository = shipmentRepository;
        this.equipamentGroupRepository = equipamentGroupRepository;
        this.equipamentRepository = equipamentRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public void processTransport(List<ApiVehicleRoute> routes, Double totalCost) {

        for(ApiVehicleRoute vehicleRoute : routes) {

            if(vehicleRoute.visits() == null || vehicleRoute.visits().isEmpty())
                continue;

            String vehicleLabel = vehicleRoute.vehicleLabel();

            Transport transport = new Transport();

            transport.setShipmentQuantity(vehicleRoute.visits().size());

            Integer totalDistance = vehicleRoute.metrics().travelDistanceMeters();
            Integer totalDuration = parseApiRouteDuration(vehicleRoute.metrics().travelDuration());
            Integer totalWait = vehicleRoute.metrics().waitDuration() != null ?
                    parseApiRouteDuration(vehicleRoute.metrics().waitDuration().toString()) : 0;

            Equipament equipament = equipamentRepository.findByPlate(vehicleLabel)
                            .orElseThrow(() -> new RuntimeException("Placa: " + vehicleLabel + " não encontrada"));

            EquipamentGroup equipamentGroup = equipamentGroupRepository.findByEquipament1Id(equipament.getId())
                            .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE,equipament.getId()));

            Company company = companyRepository.findById(equipament.getCompany().getId()).
                    orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE,equipament.getCompany().getId()));

            transport.setCalculedDistance(totalDistance);
            transport.setTotalTimeCalculed(totalDuration);
            transport.setTimeStoppedCalculed(totalWait);
            transport.setTotalCostCalculed(totalCost);
            transport.setEquipamentGroup(equipamentGroup);
            transport.setTransporter(company);
            transportRepository.save(transport);

            if (vehicleRoute.transitions() == null || vehicleRoute.transitions().isEmpty())
                continue;

            List<LatLng> totalVehiclePoints = new ArrayList<>();

            var transitions = vehicleRoute.transitions();
            var visits = vehicleRoute.visits();

            if (transitions != null && visits != null) {

                for (int i = 0; i < transitions.size(); i++) {
                    ApiRouteTransition routeTransition = transitions.get(i);

                    if (i >= visits.size()) {
                        continue;
                    }

                    if (routeTransition.routePolyline() == null ||
                            routeTransition.routePolyline().points() == null ||
                            routeTransition.routePolyline().points().isEmpty()) {
                        continue;
                    }

                    List<LatLng> optimizedPoints = OptimizeListLocation.procesingRouteGoogle(routeTransition.routePolyline().points());
                    String polylineCode = com.google.maps.internal.PolylineEncoding.encode(optimizedPoints);

                    transport.setRoutePlanned(polylineCode);

                    ApiRouteStop routeStop = visits.get(i);
                    String labelId = routeStop.shipmentLabel();

                    if(labelId != null && labelId.contains("/")) {

                        String[] ids = labelId.split("/");
                        UUID idColeta = UUID.fromString(ids[0]);
                        UUID idEntrega = UUID.fromString(ids[1]);
                        UUID shipmentId = routeStop.isPickup() ? idColeta : idEntrega;

                        Shipment shipment = shipmentRepository.findById(shipmentId)
                                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, shipmentId));

                        shipment.setTransport(transport);

                        shipmentRepository.save(shipment);
                    }
                }
            }
        }
    }

    private Integer parseApiRouteDuration (String durationStr) {
        if (durationStr == null || durationStr.isEmpty()) {
            return 0;
        }
        String number = durationStr.replace("s", "");

        return Integer.parseInt(number);
    }
}