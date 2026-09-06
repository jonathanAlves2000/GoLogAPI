package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.dtoRouteOptimization.request.*;
import GoLogAPI.dto.optimizeRoute.OptimizeRouteRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.client.RouteOptimizationClient;
import GoLogAPI.model.*;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.enums.TypeOperation;
import GoLogAPI.repository.*;
import GoLogAPI.service.MessageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
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
    private final WorkScheduleRepository workScheduleRepository;

    public RouteRequestService(EquipamentGroupRepository equipamentGroupRepository, ShipmentRepository shipmentRepository,
                               AddressRepository addressRepository, RouteOptimizationClient routeOptimizationClient,
                               TelemetryRepository telemetryRepository, TractorRepository tractorRepository,
                               WorkScheduleRepository workScheduleRepository)
    {
        this.equipamentGroupRepository = equipamentGroupRepository;
        this.shipmentRepository = shipmentRepository;
        this.routeOptimizationClient = routeOptimizationClient;
        this.telemetryRepository = telemetryRepository;
        this.tractorRepository = tractorRepository;
        this.workScheduleRepository =workScheduleRepository;
    }

    @Transactional
    public String optimizeRoutes(OptimizeRouteRequest optimizeRouteRequest){

        List<WorkSchedule> workSchedules = workScheduleRepository.findAllById(optimizeRouteRequest.workScheduleIds());

        List<Shipment> collects = shipmentRepository.findByIdInAndTypeOperation(optimizeRouteRequest.shipmentIds(), TypeOperation.COLETA);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

        List<Vehicle> vehicles = new ArrayList<>();

        for(WorkSchedule workSchedule : workSchedules) {

            EquipamentGroup equipament = workSchedule.getEquipamentGroup();
            Driver driver = workSchedule.getDriver();

            Telemetry telemetry = telemetryRepository.findTopByEquipamentIdOrderByDateTimeDesc(equipament.getEquipament1())
                    .orElse(null);

            Tractor tractor = tractorRepository.findById(equipament.getEquipament1().getId()).
                    orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipament.getEquipament1().getId()));

            LocalDate routeDate = LocalDate.now();

            OffsetDateTime startDateTime = routeDate.atTime(workSchedule.getStartWorkday()).atOffset(ZoneOffset.of("-03:00"));
            OffsetDateTime endDateTime = routeDate.atTime(workSchedule.getEndWorkday()).atOffset(ZoneOffset.of("-03:00"));

            if(workSchedule.getEndWorkday().isBefore(workSchedule.getStartWorkday())) {
                endDateTime = endDateTime.plusDays(1); // turno atravessa a meia-noite
            }

            LocalDate today = LocalDate.now();
            LocalDate validUntil = workSchedule.getScheduleDate();

            List<TimeWindow> startWindows = new ArrayList<>();
            List<TimeWindow> endWindows = new ArrayList<>();

            ZoneOffset offset = ZoneOffset.of("-03:00");

            for(LocalDate date = today; !date.isAfter(validUntil); date = date.plusDays(1)) {
                OffsetDateTime start = date.atTime(workSchedule.getStartWorkday()).atOffset(offset);
                OffsetDateTime end = date.atTime(workSchedule.getEndWorkday()).atOffset(offset);
                if (end.isBefore(start)) end = end.plusDays(1); // turno noturno

                startWindows.add(new TimeWindow(start.format(formatter), end.minusHours(2).format(formatter)));
                endWindows.add(new TimeWindow(end.format(formatter), end.plusHours(2).format(formatter)));
            }

            double kmMultiplier = switch (optimizeRouteRequest.routePriority()){
                case ECONOMIA -> 2.0;
                case EQUILIBRIO -> 1.0;
                case TEMPO -> 0.1;
            };

            double hourMultiplier = switch (optimizeRouteRequest.routePriority()){
                case ECONOMIA -> 0.1;
                case EQUILIBRIO -> 1.0;
                case TEMPO -> 2.0;
            };

            vehicles.add(
                    new Vehicle(equipament.getEquipament1().getPlate(),
                    new Location(
                            telemetry != null && !telemetry.getLatitude().isEmpty() ? telemetry.getLatitude() : equipament.getEquipament1().getCompany().getAddress().getLatitude(),
                            telemetry != null && !telemetry.getLongitude().isEmpty() ? telemetry.getLongitude() : equipament.getEquipament1().getCompany().getAddress().getLongitude()
                    ),
                    new LoadLimits(new Weight(String.valueOf(equipament.getEquipament1().getMaximumCapacity().longValue()))),
                    startWindows, // Pode inciar entre x e y
                    endWindows,  // precisa terminar entre x e y
                    tractor.getCostPerKilometer() * kmMultiplier,
                    driver.getCostPerHour() * hourMultiplier
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

        LocalDate maxValidUntil = LocalDate.now();

        for (WorkSchedule workSchedule : workSchedules) {
            if (workSchedule.getScheduleDate().isAfter(maxValidUntil)) {
                maxValidUntil = workSchedule.getScheduleDate();
            }
        }

        ZoneOffset offset = ZoneOffset.of("-03:00");

        OffsetDateTime globalStart = LocalDate.now().atStartOfDay().atOffset(offset);
        OffsetDateTime globalEnd = maxValidUntil.plusDays(2).atStartOfDay().atOffset(offset);

        String globalStartTime = globalStart.format(formatter);
        String globalEndTime = globalEnd.format(formatter);

        Model model = new Model(
                vehicles,
                shipments,
                globalStartTime,
                globalEndTime
        );

        RouteOptimizationRequest request = new RouteOptimizationRequest(model, true, true);
        String responseRoute = routeOptimizationClient.fetchOptimizedRoute(request);
        return responseRoute;
    }
}
