package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.dtoRouteOptimization.request.*;
import GoLogAPI.dto.optimizeRoute.OptimizeRouteRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.client.RouteOptimizationClient;
import GoLogAPI.model.*;
import GoLogAPI.model.enums.TypeOperation;
import GoLogAPI.model.enums.VisitTypeRuleType;
import GoLogAPI.repository.*;
import GoLogAPI.service.MessageException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class RouteRequestService {

    private final ShipmentRepository shipmentRepository;
    private final RouteOptimizationClient routeOptimizationClient;
    private final TelemetryRepository telemetryRepository;
    private final TractorRepository tractorRepository;
    private final WorkScheduleRepository workScheduleRepository;
    private final OptimizationConfigResolver configResolver;
    private final VisitTypeRuleRepository visitTypeRuleRepository;

    public RouteRequestService(
            ShipmentRepository shipmentRepository,
            RouteOptimizationClient routeOptimizationClient,
            TelemetryRepository telemetryRepository,
            TractorRepository tractorRepository,
            WorkScheduleRepository workScheduleRepository,
            OptimizationConfigResolver configResolver,
            VisitTypeRuleRepository visitTypeRuleRepository) {
        this.shipmentRepository = shipmentRepository;
        this.routeOptimizationClient = routeOptimizationClient;
        this.telemetryRepository = telemetryRepository;
        this.tractorRepository = tractorRepository;
        this.workScheduleRepository = workScheduleRepository;
        this.configResolver = configResolver;
        this.visitTypeRuleRepository = visitTypeRuleRepository;
    }

    @Transactional
    public String optimizeRoutes(OptimizeRouteRequest optimizeRouteRequest) {

        List<WorkSchedule> workSchedules = workScheduleRepository.findAllById(optimizeRouteRequest.workScheduleIds());
        List<Shipment> collects = shipmentRepository.findByIdInAndTypeOperation(optimizeRouteRequest.shipmentIds(), TypeOperation.COLETA);

        // Identifica a empresa principal da operação para resolução de perfil/regras
        Company company = null;
        if (!workSchedules.isEmpty() && workSchedules.get(0).getEquipamentGroup() != null && workSchedules.get(0).getEquipamentGroup().getEquipament1() != null) {
            company = workSchedules.get(0).getEquipamentGroup().getEquipament1().getCompany();
        } else if (!collects.isEmpty()) {
            company = collects.get(0).getCustomer();
        }

        // Resolução hierárquica de regras, custos e janelas operacionais (Request -> Perfil -> Fallback Default)
        OptimizationConfigResolver.ResolvedOptimizationSettings settings = configResolver.resolveSettings(optimizeRouteRequest, company);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
        ZoneOffset offset = ZoneOffset.of("-03:00");
        String stopDurationStr = settings.defaultServiceDurationSeconds() + "s";

        List<Vehicle> vehicles = new ArrayList<>();

        for (WorkSchedule workSchedule : workSchedules) {
            EquipamentGroup equipament = workSchedule.getEquipamentGroup();
            Driver driver = workSchedule.getDriver();

            Telemetry telemetry = telemetryRepository.findTopByEquipamentIdOrderByDateTimeDesc(equipament.getEquipament1())
                    .orElse(null);

            Tractor tractor = tractorRepository.findById(equipament.getEquipament1().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipament.getEquipament1().getId()));

            LocalDate today = LocalDate.now();
            LocalDate validUntil = workSchedule.getScheduleDate();

            List<TimeWindow> startWindows = new ArrayList<>();
            List<TimeWindow> endWindows = new ArrayList<>();

            for (LocalDate date = today; !date.isAfter(validUntil); date = date.plusDays(1)) {
                OffsetDateTime start = date.atTime(workSchedule.getStartWorkday()).atOffset(offset);
                OffsetDateTime end = date.atTime(workSchedule.getEndWorkday()).atOffset(offset);
                if (end.isBefore(start)) end = end.plusDays(1); // turno noturno

                startWindows.add(new TimeWindow(
                        start.format(formatter),
                        end.minusHours(settings.vehicleStartWindowLeadHours()).format(formatter)
                ));
                endWindows.add(new TimeWindow(
                        end.format(formatter),
                        end.plusHours(settings.vehicleEndWindowMarginHours()).format(formatter)
                ));
            }

            // Resolução dinâmica de capacidades (peso, volume, paletes, etc.) com fallback
            Map<String, LoadLimit> vehicleLimits = configResolver.resolveVehicleLoadLimits(equipament);

            vehicles.add(new Vehicle(
                    equipament.getEquipament1().getPlate(),
                    new Location(
                            telemetry != null && !telemetry.getLatitude().isEmpty() ? telemetry.getLatitude() : equipament.getEquipament1().getCompany().getAddress().getLatitude(),
                            telemetry != null && !telemetry.getLongitude().isEmpty() ? telemetry.getLongitude() : equipament.getEquipament1().getCompany().getAddress().getLongitude()
                    ),
                    new LoadLimits(vehicleLimits),
                    startWindows,
                    endWindows,
                    tractor.getCostPerKilometer() * settings.kmCostMultiplier(),
                    driver.getCostPerHour() * settings.hourCostMultiplier(),
                    settings.fixedCostPerVehicle() > 0 ? settings.fixedCostPerVehicle() : null,
                    settings.costPerTraveledHour() > 0 ? settings.costPerTraveledHour() : null,
                    null
            ));
        }

        List<RouteShipment> shipments = new ArrayList<>();
        for (Shipment collect : collects) {
            List<Shipment> deliveries = shipmentRepository.findByOperationOrigem(collect);

            List<String> collectVisitTypes = new ArrayList<>();
            if (collect.getTypeTransport() != null && collect.getTypeTransport().getName() != null) {
                collectVisitTypes.add(collect.getTypeTransport().getName().trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_"));
            }
            if (collect.getVisitTypes() != null) {
                collectVisitTypes.addAll(collect.getVisitTypes().stream().map(VisitType::getCode).toList());
            }

            Stop pickupStop = new Stop(
                    new Location(collect.getAddress().getLatitude(), collect.getAddress().getLongitude()),
                    stopDurationStr,
                    List.of(new TimeWindow(
                            collect.getSchedulind().minusMinutes(settings.timeWindowLeadMinutes()).atOffset(offset).format(formatter),
                            collect.getSchedulind().atOffset(offset).format(formatter)
                    )),
                    collectVisitTypes.isEmpty() ? null : collectVisitTypes
            );

            for (Shipment delivery : deliveries) {
                List<String> deliveryVisitTypes = new ArrayList<>();
                if (delivery.getTypeTransport() != null && delivery.getTypeTransport().getName() != null) {
                    deliveryVisitTypes.add(delivery.getTypeTransport().getName().trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_"));
                }
                if (delivery.getVisitTypes() != null) {
                    deliveryVisitTypes.addAll(delivery.getVisitTypes().stream().map(VisitType::getCode).toList());
                }

                Stop deliveryStop = new Stop(
                        new Location(delivery.getAddress().getLatitude(), delivery.getAddress().getLongitude()),
                        stopDurationStr,
                        List.of(new TimeWindow(
                                delivery.getSchedulind().minusMinutes(settings.timeWindowLeadMinutes()).atOffset(offset).format(formatter),
                                delivery.getSchedulind().atOffset(offset).format(formatter)
                        )),
                        deliveryVisitTypes.isEmpty() ? null : deliveryVisitTypes
                );

                // Resolução dinâmica de demandas de carga (peso, volume, paletes...) com fallback
                Map<String, LoadDemand> deliveryDemands = configResolver.resolveShipmentLoadDemands(delivery);

                List<PickupRequest> pickups = List.of(
                        new PickupRequest(pickupStop, new LoadDemands(deliveryDemands))
                );

                List<DeliveryRequest> deliveryRequests = List.of(
                        new DeliveryRequest(deliveryStop, new LoadDemands(deliveryDemands))
                );

                shipments.add(new RouteShipment(
                        collect.getId().toString() + "/" + delivery.getId().toString(),
                        pickups,
                        deliveryRequests,
                        settings.penaltyCostUnserved()
                ));
            }
        }

        LocalDate maxValidUntil = LocalDate.now();
        for (WorkSchedule workSchedule : workSchedules) {
            if (workSchedule.getScheduleDate().isAfter(maxValidUntil)) {
                maxValidUntil = workSchedule.getScheduleDate();
            }
        }

        OffsetDateTime globalStart = LocalDate.now().atStartOfDay().atOffset(offset);
        OffsetDateTime globalEnd = maxValidUntil.plusDays(settings.globalHorizonExtraDays()).atStartOfDay().atOffset(offset);

        String globalStartTime = globalStart.format(formatter);
        String globalEndTime = globalEnd.format(formatter);

        // Regras de incompatibilidade morfológica cadastradas no banco
        List<ShipmentTypeIncompatibility> incompatibilities = null;
        if (company != null && company.getId() != null) {
            List<VisitTypeRule> rules = visitTypeRuleRepository.findAvailableByCompanyId(company.getId());
            List<ShipmentTypeIncompatibility> mappedIncompatibilities = new ArrayList<>();
            for (VisitTypeRule rule : rules) {
                if (rule.getRuleType() == VisitTypeRuleType.INCOMPATIBLE_ON_SAME_VEHICLE) {
                    mappedIncompatibilities.add(new ShipmentTypeIncompatibility(
                            List.of(rule.getVisitType1().getCode(), rule.getVisitType2().getCode()),
                            ShipmentTypeIncompatibility.NOT_PERFORMED_BY_SAME_VEHICLE
                    ));
                }
            }
            if (!mappedIncompatibilities.isEmpty()) {
                incompatibilities = mappedIncompatibilities;
            }
        }

        Model model = new Model(
                vehicles,
                shipments,
                globalStartTime,
                globalEndTime,
                incompatibilities
        );

        RouteOptimizationRequest request = new RouteOptimizationRequest(model, true, true);
        return routeOptimizationClient.fetchOptimizedRoute(request);
    }
}
