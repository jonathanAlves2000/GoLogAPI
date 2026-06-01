package GoLogAPI.service;

import GoLogAPI.dto.dtoRouteOptimization.*;
import GoLogAPI.dto.dtoRouteOptimization.Shipment;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.client.RouteOptimizationClient;
import GoLogAPI.model.*;
import GoLogAPI.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
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

    public String optimizeRoutes(){
        List<EquipamentGroup> equipaments = equipamentGroupRepository.findAll();
        List<GoLogAPI.model.Shipment> shipment = shipmentRepository.findAll();
        List<Address> addresses = addressRepository.findAll();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");


        List<Vehicle> vehicles = equipaments.stream()
                .map(equipament -> {
                    Telemetry telemetry = telemetryRepository.findTopByEquipamentIdOrderByDateTimeDesc(equipament.getEquipament1())
                            .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipament.getEquipament1().getId()));

                    return new Vehicle(
                            equipament.getEquipament1().getPlate(),

                            //Localicação inicial
                            new Location(
                                    telemetry.getLatitude(),
                                    telemetry.getLongitude()
                            ),

                            //Limite de carga
                            new LoadLimits(
                                    new Weight(String.valueOf(equipament.getEquipament1().getMaximumCapacity().longValue()))
                            )
                    );
                })
                .toList();

        List<Shipment> shipments = shipment.stream()
                .map(shipmentDto -> {

                   return new Shipment(
                           String.valueOf(shipmentDto.getId()),

                            shipmentDto.getTypeOperation() == TypeOperation.COLETA ?
                                    List.of(
                                            new Stop(
                                                    new Location(
                                                            shipmentDto.getAddress().getLatitude(),
                                                            shipmentDto.getAddress().getLongitude()
                                                    ),
                                                    "3600s",
                                                    List.of(
                                                            new TimeWindow(
                                                                    shipmentDto.getSchedulind()
                                                                            .minusMinutes(15)
                                                                            .atOffset(ZoneOffset.of("-03:00"))
                                                                            .format(formatter),
                                                                    shipmentDto.getSchedulind()
                                                                            .atOffset(ZoneOffset.of("-03:00"))
                                                                            .format(formatter)
                                                                    )
                                                    )
                                            )
                                    )
                            : null,
                            shipmentDto.getTypeOperation() == TypeOperation.ENTREGA ?
                                List.of(new Stop(
                                            new Location(
                                                    shipmentDto.getAddress().getLatitude(),
                                                    shipmentDto.getAddress().getLongitude()
                                            ),
                                            "3600s",
                                            List.of(
                                                    new TimeWindow(
                                                            shipmentDto.getSchedulind()
                                                                    .minusMinutes(15)
                                                                    .atOffset(ZoneOffset.of("-03:00"))
                                                                    .format(formatter),
                                                            shipmentDto.getSchedulind()
                                                                    .atOffset(ZoneOffset.of("-03:00"))
                                                                    .format(formatter)
                                                    )
                                            )
                                        )
                                )
                            : null,
                            new LoadDemands(
                                    new WeightAmount(String.valueOf(shipmentDto.getWeight().longValue()))
                            )
                    );
                })
                .toList();

        Model model = new Model(
                vehicles,
                shipments
        );

        LocalDate date = shipment.getFirst().getSchedulind().toLocalDate();

        String globalStartTime =date.atTime(0, 0)
                .atOffset(ZoneOffset.of("-03:00"))
                .format(formatter);

        String globalEndTime = date.atTime(23, 59, 59)
                        .atOffset(ZoneOffset.of("-03:00"))
                        .format(formatter);

        RouteOptimizationRequest request = new RouteOptimizationRequest(
                model,
                globalStartTime,
                globalEndTime
        );

        System.out.println(request);

        String response = routeOptimizationClient.fetchOptimizedRoute(request);

        System.out.println(response);

        return response;
    }
}
