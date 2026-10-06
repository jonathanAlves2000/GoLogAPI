package GoLogAPI.service.routeOptimization;

import GoLogAPI.dto.optimizeRoute.OptimizeRouteRequest;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.client.RouteOptimizationClient;
import GoLogAPI.model.*;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.enums.TypeOperation;
import GoLogAPI.model.enums.VisitTypeRuleType;
import GoLogAPI.repository.*;
import GoLogAPI.service.MessageException;
import com.google.cloud.optimization.v1.OptimizeToursRequest;
import com.google.cloud.optimization.v1.OptimizeToursResponse;
import com.google.cloud.optimization.v1.ShipmentModel;
import com.google.cloud.optimization.v1.TimeWindow;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import com.google.type.LatLng;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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
    public OptimizeToursResponse optimizeRoutes(OptimizeRouteRequest optimizeRouteRequest) {

        List<WorkSchedule> workSchedules = workScheduleRepository.findAllById(optimizeRouteRequest.workScheduleIds());
        List<Shipment> collects = shipmentRepository.findByIdInAndTypeOperation(optimizeRouteRequest.shipmentIds(), TypeOperation.COLETA);

        Company company = null;
        if (!workSchedules.isEmpty() && workSchedules.get(0).getEquipamentGroup() != null && workSchedules.get(0).getEquipamentGroup().getEquipament1() != null) {
            company = workSchedules.get(0).getEquipamentGroup().getEquipament1().getCompany();
        } else if (!collects.isEmpty()) {
            company = collects.get(0).getCustomer();
        }

        OptimizationConfigResolver.ResolvedOptimizationSettings settings = configResolver.resolveSettings(optimizeRouteRequest, company);
        ZoneOffset offset = ZoneOffset.of("-03:00");
        Duration stopDuration = Duration.newBuilder().setSeconds(settings.defaultServiceDurationSeconds()).build();

        ShipmentModel.Builder shipmentModelBuilder = ShipmentModel.newBuilder();

        // 1. Montagem dos Veículos usando classes nativas do protobuf
        for (WorkSchedule workSchedule : workSchedules) {
            EquipamentGroup equipament = workSchedule.getEquipamentGroup();
            Driver driver = workSchedule.getDriver();

            Telemetry telemetry = telemetryRepository.findTopByEquipamentIdOrderByDateTimeDesc(equipament.getEquipament1())
                    .orElse(null);

            Tractor tractor = tractorRepository.findById(equipament.getEquipament1().getId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipament.getEquipament1().getId()));

            String latStr = (telemetry != null && !telemetry.getLatitude().isEmpty())
                    ? telemetry.getLatitude()
                    : equipament.getEquipament1().getCompany().getAddress().getLatitude();
            String lngStr = (telemetry != null && !telemetry.getLongitude().isEmpty())
                    ? telemetry.getLongitude()
                    : equipament.getEquipament1().getCompany().getAddress().getLongitude();

            LatLng startLocation = LatLng.newBuilder()
                    .setLatitude(Double.parseDouble(latStr))
                    .setLongitude(Double.parseDouble(lngStr))
                    .build();

            com.google.cloud.optimization.v1.Vehicle.Builder vehicleBuilder = com.google.cloud.optimization.v1.Vehicle.newBuilder()
                    .setLabel(equipament.getEquipament1().getPlate())
                    .setStartLocation(startLocation)
                    .setCostPerKilometer(tractor.getCostPerKilometer() * settings.kmCostMultiplier())
                    .setCostPerHour(driver.getCostPerHour() * settings.hourCostMultiplier());

            if (settings.fixedCostPerVehicle() > 0) {
                vehicleBuilder.setFixedCost(settings.fixedCostPerVehicle());
            }
            if (settings.costPerTraveledHour() > 0) {
                vehicleBuilder.setCostPerTraveledHour(settings.costPerTraveledHour());
            }

            // Janelas de tempo do veículo
            LocalDate today = LocalDate.now();
            LocalDate validUntil = workSchedule.getScheduleDate();
            for (LocalDate date = today; !date.isAfter(validUntil); date = date.plusDays(1)) {
                OffsetDateTime start = date.atTime(workSchedule.getStartWorkday()).atOffset(offset);
                OffsetDateTime end = date.atTime(workSchedule.getEndWorkday()).atOffset(offset);
                if (end.isBefore(start)) end = end.plusDays(1);

                Instant startInstant = start.toInstant();
                Instant endMinusLead = end.minusHours(settings.vehicleStartWindowLeadHours()).toInstant();

                vehicleBuilder.addStartTimeWindows(TimeWindow.newBuilder()
                        .setStartTime(Timestamp.newBuilder().setSeconds(startInstant.getEpochSecond()).setNanos(startInstant.getNano()))
                        .setEndTime(Timestamp.newBuilder().setSeconds(endMinusLead.getEpochSecond()).setNanos(endMinusLead.getNano()))
                        .build());

                Instant endInstant = end.toInstant();
                Instant endPlusMargin = end.plusHours(settings.vehicleEndWindowMarginHours()).toInstant();

                vehicleBuilder.addEndTimeWindows(TimeWindow.newBuilder()
                        .setStartTime(Timestamp.newBuilder().setSeconds(endInstant.getEpochSecond()).setNanos(endInstant.getNano()))
                        .setEndTime(Timestamp.newBuilder().setSeconds(endPlusMargin.getEpochSecond()).setNanos(endPlusMargin.getNano()))
                        .build());
            }

            // Capacidades do veículo
            var vehicleLimits = configResolver.resolveVehicleLoadLimits(equipament);
            for (var entry : vehicleLimits.entrySet()) {
                long max = entry.getValue().maxLoad() != null ? Long.parseLong(entry.getValue().maxLoad()) : 0L;
                vehicleBuilder.putLoadLimits(entry.getKey(), com.google.cloud.optimization.v1.Vehicle.LoadLimit.newBuilder()
                        .setMaxLoad(max)
                        .build());
            }

            shipmentModelBuilder.addVehicles(vehicleBuilder.build());
        }

        // 2. Montagem dos Shipments
        for (Shipment collect : collects) {
            List<Shipment> deliveries = shipmentRepository.findByOperationOrigem(collect);

            List<String> collectVisitTypes = new ArrayList<>();
            if (collect.getTypeTransport() != null && collect.getTypeTransport().getName() != null) {
                collectVisitTypes.add(collect.getTypeTransport().getName().trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_"));
            }
            if (collect.getVisitTypes() != null) {
                collectVisitTypes.addAll(collect.getVisitTypes().stream().map(VisitType::getCode).toList());
            }

            LatLng pickupLocation = LatLng.newBuilder()
                    .setLatitude(Double.parseDouble(collect.getAddress().getLatitude()))
                    .setLongitude(Double.parseDouble(collect.getAddress().getLongitude()))
                    .build();

            Instant collectInstant = collect.getSchedulind().atOffset(offset).toInstant();
            Instant collectMinusLead = collect.getSchedulind().minusMinutes(settings.timeWindowLeadMinutes()).atOffset(offset).toInstant();

            TimeWindow pickupWindow = TimeWindow.newBuilder()
                    .setStartTime(Timestamp.newBuilder().setSeconds(collectMinusLead.getEpochSecond()).setNanos(collectMinusLead.getNano()))
                    .setEndTime(Timestamp.newBuilder().setSeconds(collectInstant.getEpochSecond()).setNanos(collectInstant.getNano()))
                    .build();

            for (Shipment delivery : deliveries) {
                List<String> deliveryVisitTypes = new ArrayList<>();
                if (delivery.getTypeTransport() != null && delivery.getTypeTransport().getName() != null) {
                    deliveryVisitTypes.add(delivery.getTypeTransport().getName().trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_"));
                }
                if (delivery.getVisitTypes() != null) {
                    deliveryVisitTypes.addAll(delivery.getVisitTypes().stream().map(VisitType::getCode).toList());
                }

                LatLng deliveryLocation = LatLng.newBuilder()
                        .setLatitude(Double.parseDouble(delivery.getAddress().getLatitude()))
                        .setLongitude(Double.parseDouble(delivery.getAddress().getLongitude()))
                        .build();

                Instant deliveryInstant = delivery.getSchedulind().atOffset(offset).toInstant();
                Instant deliveryMinusLead = delivery.getSchedulind().minusMinutes(settings.timeWindowLeadMinutes()).atOffset(offset).toInstant();

                TimeWindow deliveryWindow = TimeWindow.newBuilder()
                        .setStartTime(Timestamp.newBuilder().setSeconds(deliveryMinusLead.getEpochSecond()).setNanos(deliveryMinusLead.getNano()))
                        .setEndTime(Timestamp.newBuilder().setSeconds(deliveryInstant.getEpochSecond()).setNanos(deliveryInstant.getNano()))
                        .build();

                var deliveryDemands = configResolver.resolveShipmentLoadDemands(delivery);
                Map<String, com.google.cloud.optimization.v1.Shipment.Load> protoDemands = new HashMap<>();
                for (var demandEntry : deliveryDemands.entrySet()) {
                    long amount = demandEntry.getValue().amount() != null ? Long.parseLong(demandEntry.getValue().amount()) : 0L;
                    protoDemands.put(demandEntry.getKey(), com.google.cloud.optimization.v1.Shipment.Load.newBuilder()
                            .setAmount(amount)
                            .build());
                }

                com.google.cloud.optimization.v1.Shipment.VisitRequest.Builder pickupReqBuilder = com.google.cloud.optimization.v1.Shipment.VisitRequest.newBuilder()
                        .setArrivalLocation(pickupLocation)
                        .setDuration(stopDuration)
                        .addTimeWindows(pickupWindow)
                        .putAllLoadDemands(protoDemands);
                if (!collectVisitTypes.isEmpty()) {
                    pickupReqBuilder.addAllVisitTypes(collectVisitTypes);
                }

                com.google.cloud.optimization.v1.Shipment.VisitRequest.Builder deliveryReqBuilder = com.google.cloud.optimization.v1.Shipment.VisitRequest.newBuilder()
                        .setArrivalLocation(deliveryLocation)
                        .setDuration(stopDuration)
                        .addTimeWindows(deliveryWindow)
                        .putAllLoadDemands(protoDemands);
                if (!deliveryVisitTypes.isEmpty()) {
                    deliveryReqBuilder.addAllVisitTypes(deliveryVisitTypes);
                }

                com.google.cloud.optimization.v1.Shipment protoShipment = com.google.cloud.optimization.v1.Shipment.newBuilder()
                        .setLabel(collect.getId().toString() + "/" + delivery.getId().toString())
                        .addPickups(pickupReqBuilder.build())
                        .addDeliveries(deliveryReqBuilder.build())
                        .setPenaltyCost(settings.penaltyCostUnserved())
                        .build();

                shipmentModelBuilder.addShipments(protoShipment);
            }
        }

        // Horizonte Global
        LocalDate maxValidUntil = LocalDate.now();
        for (WorkSchedule workSchedule : workSchedules) {
            if (workSchedule.getScheduleDate().isAfter(maxValidUntil)) {
                maxValidUntil = workSchedule.getScheduleDate();
            }
        }

        Instant globalStartInstant = LocalDate.now().atStartOfDay().atOffset(offset).toInstant();
        Instant globalEndInstant = maxValidUntil.plusDays(settings.globalHorizonExtraDays()).atStartOfDay().atOffset(offset).toInstant();

        shipmentModelBuilder.setGlobalStartTime(Timestamp.newBuilder()
                .setSeconds(globalStartInstant.getEpochSecond())
                .setNanos(globalStartInstant.getNano()));
        shipmentModelBuilder.setGlobalEndTime(Timestamp.newBuilder()
                .setSeconds(globalEndInstant.getEpochSecond())
                .setNanos(globalEndInstant.getNano()));

        // Incompatibilidades de Tipos
        if (company != null && company.getId() != null) {
            List<VisitTypeRule> rules = visitTypeRuleRepository.findAvailableByCompanyId(company.getId());
            for (VisitTypeRule rule : rules) {
                if (rule.getRuleType() == VisitTypeRuleType.INCOMPATIBLE_ON_SAME_VEHICLE) {
                    shipmentModelBuilder.addShipmentTypeIncompatibilities(com.google.cloud.optimization.v1.ShipmentTypeIncompatibility.newBuilder()
                            .addTypes(rule.getVisitType1().getCode())
                            .addTypes(rule.getVisitType2().getCode())
                            .setIncompatibilityMode(com.google.cloud.optimization.v1.ShipmentTypeIncompatibility.IncompatibilityMode.NOT_PERFORMED_BY_SAME_VEHICLE)
                            .build());
                }
            }
        }

        OptimizeToursRequest request = OptimizeToursRequest.newBuilder()
                .setParent("projects/" + routeOptimizationClient.getProjectId())
                .setModel(shipmentModelBuilder.build())
                .setPopulatePolylines(true)
                .setPopulateTransitionPolylines(true)
                .build();

        return routeOptimizationClient.optimizeTours(request);
    }
}
