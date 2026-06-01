package GoLogAPI.service;

import GoLogAPI.dto.dtoRouteOptimization.*;
import GoLogAPI.dto.dtoRouteOptimization.ShipmentDto;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.client.RouteOptimizationClient;
import GoLogAPI.model.*;
import GoLogAPI.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteOptimizationService {

    private final EquipamentGroupRepository equipamentGroupRepository;
    private final ShipmentRepository shipmentRepository;
    private final AddressRepository addressRepository;
    private final RouteOptimizationClient routeOptimizationClient;
    private final TelemetryRepository telemetryRepository;

    public RouteOptimizationService(EquipamentGroupRepository equipamentGroupRepository, ShipmentRepository shipmentRepository,
                                    AddressRepository addressRepository, RouteOptimizationClient routeOptimizationClient, TelemetryRepository telemetryRepository)
    {
        this.equipamentGroupRepository = equipamentGroupRepository;
        this.shipmentRepository = shipmentRepository;
        this.addressRepository = addressRepository;
        this.routeOptimizationClient = routeOptimizationClient;
        this.telemetryRepository = telemetryRepository;
    }

    public void optimizeRoutes(){
        List<EquipamentGroup> equipaments = equipamentGroupRepository.findAll();
        List<Shipment> shipment = shipmentRepository.findAll();
        List<Address> addresses = addressRepository.findAll();

        List<Vehicle> vehicles = equipaments.stream()
                .map(equipament -> {
                    Telemetry telemetry = telemetryRepository.findById(equipament.getId())
                            .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipament.getId()));

                    return new Vehicle(
                            equipament.getEquipament1().getPlate(),

                            //Localicação inicial
                            new Location(
                                    telemetry.getLatitude(),
                                    telemetry.getLongitude()
                            ),

                            //Limite de carga
                            new LoadLimits(
                                    new Weight(String.valueOf(equipament.getEquipament1().getMaximumCapacity()))
                            )
                    );
                })
                .toList();

        List<ShipmentDto> shipmentsList = shipment.stream()
                .map(shipmentDto -> {

                   return new ShipmentDto(
                           String.valueOf(shipmentDto.getId()),
                            null,
                            List.of(new Stop(
                                        new Location(
                                                shipmentDto.getAddress().getLatitude(),
                                                shipmentDto.getAddress().getLongitude()
                                        ),
                                        "3600",
                                        List.of(
                                                new TimeWindow(
                                                        shipmentDto.getSchedulind()
                                                                .minusMinutes(15)
                                                                .toString(),
                                                        String.valueOf(shipmentDto.getSchedulind())
                                                )
                                        )
                                    )
                            ),
                            new LoadDemands(
                                    new WeightAmount(String.valueOf(shipmentDto.getWeight()))
                            )
                    );
                })
                .toList();
    }
}
