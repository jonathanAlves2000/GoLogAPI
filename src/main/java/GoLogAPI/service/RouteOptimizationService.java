package GoLogAPI.service;

import GoLogAPI.dto.dtoRouteOptimization.*;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.client.RouteOptimizationClient;
import GoLogAPI.model.*;
import GoLogAPI.model.Shipment;
import GoLogAPI.repository.*;
import org.springframework.stereotype.Service;
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
        List<Shipment> collects = shipmentRepository.findByTypeOperation(TypeOperation.COLETA);
        List<Address> addresses = addressRepository.findAll();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");


        List<Vehicle> vehicles = equipaments.stream()
                .map(equipament -> {
                    Telemetry telemetry = telemetryRepository.findTopByEquipamentIdOrderByDateTimeDesc(equipament.getEquipament1())
                            .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipament.getEquipament1().getId()));

                    return new Vehicle(
                            equipament.getEquipament1().getPlate(),
                            new Location(telemetry.getLatitude(), telemetry.getLongitude()),
                            new LoadLimits(new Weight(String.valueOf(equipament.getEquipament1().getMaximumCapacity().longValue()))),
                            List.of(new TimeWindow("2026-06-11T00:00:00-03:00", "2026-06-11T00:00:00-03:00")),
                            List.of(new TimeWindow("2026-06-11T23:59:59-03:00", "2026-06-11T23:59:59-03:00"))
                    );
                })
                .toList();

        List<RouteShipment> shipments = collects.stream()
                .map(collect -> {

                    List<Shipment> deliveries = shipmentRepository.findByOperationOrigem(collect);

                    Stop pickupStop = new Stop(
                            new Location(collect.getAddress().getLatitude(), collect.getAddress().getLongitude()),
                            "3600s",
                            List.of(new TimeWindow(
                                    collect.getSchedulind().minusHours(1).atOffset(ZoneOffset.of("-03:00")).format(formatter),
                                    collect.getSchedulind().atOffset(ZoneOffset.of("-03:00")).format(formatter)
                            ))
                    );

                    List<PickupRequest> pickups = List.of(
                            new PickupRequest(
                                    pickupStop,
                                    new LoadDemands(new WeightAmount(String.valueOf(collect.getWeight().longValue())))
                            )
                    );

                    List<DeliveryRequest> deliveryRequests = deliveries.stream()
                            .map(delivery -> {

                                Stop deliveryStop = new Stop(
                                        new Location(delivery.getAddress().getLatitude(), delivery.getAddress().getLongitude()),
                                        "3600s",
                                        List.of(new TimeWindow(
                                                delivery.getSchedulind().minusHours(1).atOffset(ZoneOffset.of("-03:00")).format(formatter),
                                                delivery.getSchedulind().atOffset(ZoneOffset.of("-03:00")).format(formatter)
                                        ))
                                );

                                long negativeWeight = Math.abs(delivery.getWeight().longValue());

                                return new DeliveryRequest(
                                        deliveryStop,
                                        new LoadDemands(new WeightAmount(String.valueOf(negativeWeight)))
                                );
                            })
                            .toList();

                    return new RouteShipment(
                            collect.getId().toString(),
                            pickups,
                            deliveryRequests
                    );
                })
                .toList();

        String globalStartTime = "2026-06-11T00:00:00-03:00";
        String globalEndTime = "2026-06-11T23:59:59-03:00";
        ValueFunction valueFunction = new ValueFunction(1.0);
        Objective distanceObjective = new Objective(valueFunction);

        Model model = new Model(
                vehicles,
                shipments,
                globalStartTime,
                globalEndTime
        );

        RouteOptimizationRequest request = new RouteOptimizationRequest(model, true);
        String response = routeOptimizationClient.fetchOptimizedRoute(request);
        return response;
    }
}
