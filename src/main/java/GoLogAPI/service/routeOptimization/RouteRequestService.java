package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.dtoRouteOptimization.request.*;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.client.RouteOptimizationClient;
import GoLogAPI.model.*;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.enums.TypeOperation;
import GoLogAPI.repository.*;
import GoLogAPI.service.MessageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RouteRequestService {

    private final EquipamentGroupRepository equipamentGroupRepository;
    private final ShipmentRepository shipmentRepository;
    private final RouteOptimizationClient routeOptimizationClient;
    private final TelemetryRepository telemetryRepository;
    private final TractorRepository tractorRepository;

    public RouteRequestService(EquipamentGroupRepository equipamentGroupRepository, ShipmentRepository shipmentRepository,
                               AddressRepository addressRepository, RouteOptimizationClient routeOptimizationClient,
                               TelemetryRepository telemetryRepository, TractorRepository tractorRepository)
    {
        this.equipamentGroupRepository = equipamentGroupRepository;
        this.shipmentRepository = shipmentRepository;
        this.routeOptimizationClient = routeOptimizationClient;
        this.telemetryRepository = telemetryRepository;
        this.tractorRepository = tractorRepository;
    }

    @Transactional
    public String optimizeRoutes(){
        List<EquipamentGroup> equipaments = equipamentGroupRepository.findAll();
        List<Shipment> collects = shipmentRepository.findByTypeOperation(TypeOperation.COLETA);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        List<Vehicle> vehicles = new ArrayList<>();

        for(EquipamentGroup equipament : equipaments) {
            Telemetry telemetry = telemetryRepository.findTopByEquipamentIdOrderByDateTimeDesc(equipament.getEquipament1())
                    .orElse(null);

            Tractor tractor = tractorRepository.findById(equipament.getEquipament1().getId()).
                    orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipament.getEquipament1().getId()));

            vehicles.add(
                    new Vehicle(equipament.getEquipament1().getPlate(),
                    new Location(
                            telemetry != null && !telemetry.getLatitude().isEmpty() ? telemetry.getLatitude() : equipament.getEquipament1().getCompany().getAddress().getLatitude(),
                            telemetry != null && !telemetry.getLongitude().isEmpty() ? telemetry.getLongitude() : equipament.getEquipament1().getCompany().getAddress().getLongitude()
                    ),
                    new LoadLimits(new Weight(String.valueOf(equipament.getEquipament1().getMaximumCapacity().longValue()))),
                    List.of(new TimeWindow("2026-06-11T05:00:00-03:00", "2026-06-11T20:00:00-03:00")),
                    List.of(new TimeWindow("2026-06-11T21:00:00-03:00", "2026-06-11T23:00:00-03:00")),
                    tractor.getCo2PerKm()
            ));
        }

        List<RouteShipment> shipments = new ArrayList<>();
        for(Shipment collect : collects) {

            List<Shipment> deliveries = shipmentRepository.findByOperationOrigem(collect);

            Stop pickupStop = new Stop(
                    new Location(collect.getAddress().getLatitude(), collect.getAddress().getLongitude()),
                    "1800s",
                    List.of(new TimeWindow(
                            collect.getSchedulind().minusMinutes(15).atOffset(ZoneOffset.of("-03:00")).format(formatter),
                            collect.getSchedulind().atOffset(ZoneOffset.of("-03:00")).format(formatter)
                    ))
            );

            for(Shipment delivery : deliveries) {

                Stop deliveryStop = new Stop(
                        new Location(delivery.getAddress().getLatitude(), delivery.getAddress().getLongitude()),
                        "1800s",
                        List.of(new TimeWindow(
                                delivery.getSchedulind().minusMinutes(15).atOffset(ZoneOffset.of("-03:00")).format(formatter),
                                delivery.getSchedulind().atOffset(ZoneOffset.of("-03:00")).format(formatter)
                        ))
                );

                List<PickupRequest> pickups = List.of(
                        new PickupRequest(
                                pickupStop,
                                new LoadDemands(new WeightAmount(String.valueOf(delivery.getWeight().longValue())))
                        )
                );

                List<DeliveryRequest> deliveryRequests = List.of(
                        new DeliveryRequest(
                                deliveryStop,
                                new LoadDemands(new WeightAmount(String.valueOf(Math.abs(delivery.getWeight().longValue()))))
                        )
                );

                shipments.add(new RouteShipment(
                        collect.getId().toString() + "/" + delivery.getId().toString(),
                        pickups,
                        deliveryRequests,
                        100000.0
                ));
            }
        }

        LocalDateTime globalStart = LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0)
                .truncatedTo(ChronoUnit.SECONDS);

        LocalDateTime globalEnd = globalStart.plusDays(20);

        ZoneOffset offset = ZoneOffset.of("-03:00");
        String globalStartTime = globalStart.atOffset(offset).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String globalEndTime = globalEnd.atOffset(offset).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Model model = new Model(
                vehicles,
                shipments,
                globalStartTime,
                globalEndTime
        );

        RouteOptimizationRequest request = new RouteOptimizationRequest(model, true, true);
        String response = routeOptimizationClient.fetchOptimizedRoute(request);
        return response;
    }
}
