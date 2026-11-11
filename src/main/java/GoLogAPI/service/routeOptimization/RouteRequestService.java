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
import com.google.maps.routeoptimization.v1.BreakRule;
import com.google.maps.routeoptimization.v1.OptimizeToursRequest;
import com.google.maps.routeoptimization.v1.OptimizeToursResponse;
import com.google.maps.routeoptimization.v1.ShipmentModel;
import com.google.maps.routeoptimization.v1.TimeWindow;
import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import com.google.type.LatLng;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
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
    private final GoLogAPI.repository.CompanyRepository companyRepository;

    public RouteRequestService(
            ShipmentRepository shipmentRepository,
            RouteOptimizationClient routeOptimizationClient,
            TelemetryRepository telemetryRepository,
            TractorRepository tractorRepository,
            WorkScheduleRepository workScheduleRepository,
            OptimizationConfigResolver configResolver,
            VisitTypeRuleRepository visitTypeRuleRepository,
            GoLogAPI.repository.CompanyRepository companyRepository) {
        this.shipmentRepository = shipmentRepository;
        this.routeOptimizationClient = routeOptimizationClient;
        this.telemetryRepository = telemetryRepository;
        this.tractorRepository = tractorRepository;
        this.workScheduleRepository = workScheduleRepository;
        this.configResolver = configResolver;
        this.visitTypeRuleRepository = visitTypeRuleRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public OptimizeToursResponse optimizeRoutes(OptimizeRouteRequest optimizeRouteRequest) {

        List<WorkSchedule> workSchedules = workScheduleRepository.findAllById(optimizeRouteRequest.workScheduleIds());
        List<Shipment> requestedShipments = shipmentRepository.findAllById(optimizeRouteRequest.shipmentIds());

        Company company = null;
        if (!workSchedules.isEmpty() && workSchedules.get(0).getEquipamentGroup() != null && workSchedules.get(0).getEquipamentGroup().getEquipament1() != null) {
            company = workSchedules.get(0).getEquipamentGroup().getEquipament1().getCompany();
        }
        if (company == null && !requestedShipments.isEmpty()) {
            company = requestedShipments.get(0).getCustomer();
        }
        if (company == null) {
            company = companyRepository.findAll().stream().findFirst().orElse(null);
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

            String latStr = "-23.550520";
            String lngStr = "-46.633308";
            if (telemetry != null && telemetry.getLatitude() != null && !telemetry.getLatitude().isBlank()) {
                latStr = telemetry.getLatitude();
            } else if (equipament.getEquipament1() != null && equipament.getEquipament1().getCompany() != null && equipament.getEquipament1().getCompany().getAddress() != null) {
                if (equipament.getEquipament1().getCompany().getAddress().getLatitude() != null) {
                    latStr = equipament.getEquipament1().getCompany().getAddress().getLatitude();
                }
            }
            if (telemetry != null && telemetry.getLongitude() != null && !telemetry.getLongitude().isBlank()) {
                lngStr = telemetry.getLongitude();
            } else if (equipament.getEquipament1() != null && equipament.getEquipament1().getCompany() != null && equipament.getEquipament1().getCompany().getAddress() != null) {
                if (equipament.getEquipament1().getCompany().getAddress().getLongitude() != null) {
                    lngStr = equipament.getEquipament1().getCompany().getAddress().getLongitude();
                }
            }

            LatLng startLocation = LatLng.newBuilder()
                    .setLatitude(Double.parseDouble(latStr))
                    .setLongitude(Double.parseDouble(lngStr))
                    .build();

            com.google.maps.routeoptimization.v1.Vehicle.Builder vehicleBuilder = com.google.maps.routeoptimization.v1.Vehicle.newBuilder()
                    .setLabel(equipament.getEquipament1().getPlate() + "/" + equipament.getId() + "/" + workSchedule.getId())
                    .setStartLocation(startLocation)
                    .setCostPerKilometer(tractor.getCostPerKilometer() * settings.kmCostMultiplier())
                    .setCostPerHour(driver.getCostPerHour() * settings.hourCostMultiplier());

            if (settings.fixedCostPerVehicle() > 0) {
                vehicleBuilder.setFixedCost(settings.fixedCostPerVehicle());
            }
            if (settings.costPerTraveledHour() > 0) {
                vehicleBuilder.setCostPerTraveledHour(settings.costPerTraveledHour());
            }

            // Janelas de tempo do veículo: cobre a data da escala e os dias de coleta/entrega solicitados
            java.util.TreeSet<LocalDate> operatingDates = new java.util.TreeSet<>();
            if (workSchedule.getScheduleDate() != null) {
                operatingDates.add(workSchedule.getScheduleDate());
            }
            for (Shipment s : requestedShipments) {
                if (s.getSchedulind() != null) {
                    operatingDates.add(s.getSchedulind().toLocalDate());
                }
                if (s.getOperationOrigem() != null && s.getOperationOrigem().getSchedulind() != null) {
                    operatingDates.add(s.getOperationOrigem().getSchedulind().toLocalDate());
                }
            }
            LocalDate today = LocalDate.now();
            if (workSchedule.getScheduleDate() != null && !today.isAfter(workSchedule.getScheduleDate())) {
                for (LocalDate date = today; !date.isAfter(workSchedule.getScheduleDate()); date = date.plusDays(1)) {
                    operatingDates.add(date);
                }
            }
            if (operatingDates.isEmpty()) {
                operatingDates.add(today);
            }

            for (LocalDate date : operatingDates) {
                OffsetDateTime start = date.atTime(workSchedule.getStartWorkday()).atOffset(offset);
                OffsetDateTime end = date.atTime(workSchedule.getEndWorkday()).atOffset(offset);
                if (end.isBefore(start)) end = end.plusDays(1);

                Instant startInstant = start.toInstant();
                Instant endMinusLead = end.minusHours(settings.vehicleStartWindowLeadHours()).toInstant();
                if (endMinusLead.isBefore(startInstant)) {
                    endMinusLead = startInstant.plusSeconds(3600);
                }

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
                vehicleBuilder.putLoadLimits(entry.getKey(), com.google.maps.routeoptimization.v1.Vehicle.LoadLimit.newBuilder()
                        .setMaxLoad(max)
                        .build());
            }

            // Regras de Jornada e Intervalos do Motorista (Lei nº 13.103 / Intervalos Flexíveis)
            BreakRule.Builder breakRuleBuilder = BreakRule.newBuilder();

            // 1. Descanso periódico obrigatório da Lei do Motorista (máx horas ininterruptas de direção)
            if (workSchedule.getMaxDrivingHoursWithoutBreak() != null && workSchedule.getMaxDrivingHoursWithoutBreak() > 0) {
                long maxDrivingSecs = workSchedule.getMaxDrivingHoursWithoutBreak() * 3600L;
                long minDrivingBreakSecs = (workSchedule.getMinDrivingBreakMinutes() != null && workSchedule.getMinDrivingBreakMinutes() > 0)
                        ? workSchedule.getMinDrivingBreakMinutes() * 60L
                        : 1800L; // 30 minutos

                breakRuleBuilder.addFrequencyConstraints(BreakRule.FrequencyConstraint.newBuilder()
                        .setMaxInterBreakDuration(Duration.newBuilder().setSeconds(maxDrivingSecs).build())
                        .setMinBreakDuration(Duration.newBuilder().setSeconds(minDrivingBreakSecs).build())
                        .build());
            }

            // 2. Janelas de refeição / intervalo principal
            Integer breakDuration = workSchedule.getBreakDurationMinutes();
            if (breakDuration != null && breakDuration > 0) {
                for (LocalDate date : operatingDates) {
                    LocalTime earliest = workSchedule.getEarliestBreakTime();
                    LocalTime latest = workSchedule.getLatestBreakTime();

                    OffsetDateTime earliestDt;
                    OffsetDateTime latestDt;

                    if (earliest != null && latest != null) {
                        earliestDt = date.atTime(earliest).atOffset(offset);
                        latestDt = date.atTime(latest).atOffset(offset);
                        if (latestDt.isBefore(earliestDt)) {
                            latestDt = latestDt.plusDays(1);
                        }
                    } else {
                        // Calcula meio do turno como janela aproximada caso horários específicos não tenham sido definidos
                        LocalTime startWork = workSchedule.getStartWorkday();
                        LocalTime endWork = workSchedule.getEndWorkday();
                        long shiftSecs = java.time.Duration.between(startWork, endWork.isBefore(startWork) ? endWork.plusHours(24) : endWork).getSeconds();
                        long midSecs = shiftSecs / 2;
                        OffsetDateTime midShift = date.atTime(startWork).atOffset(offset).plusSeconds(midSecs);
                        earliestDt = midShift.minusMinutes(60);
                        latestDt = midShift.plusMinutes(60);
                    }

                    Instant earliestInstant = earliestDt.toInstant();
                    Instant latestInstant = latestDt.toInstant();

                    breakRuleBuilder.addBreakRequests(BreakRule.BreakRequest.newBuilder()
                            .setEarliestStartTime(Timestamp.newBuilder().setSeconds(earliestInstant.getEpochSecond()).setNanos(earliestInstant.getNano()).build())
                            .setLatestStartTime(Timestamp.newBuilder().setSeconds(latestInstant.getEpochSecond()).setNanos(latestInstant.getNano()).build())
                            .setMinDuration(Duration.newBuilder().setSeconds(breakDuration * 60L).build())
                            .build());
                }
            }

            if (breakRuleBuilder.getBreakRequestsCount() > 0 || breakRuleBuilder.getFrequencyConstraintsCount() > 0) {
                vehicleBuilder.setBreakRule(breakRuleBuilder.build());
            }

            shipmentModelBuilder.addVehicles(vehicleBuilder.build());
        }

        // 2. Montagem dos Shipments (suporta COLETA com filhas, COLETA avulsa e ENTREGA avulsa/direta)
        List<Shipment> collects = requestedShipments.stream()
                .filter(s -> s.getTypeOperation() == TypeOperation.COLETA)
                .toList();

        java.util.Set<UUID> handledDeliveryIds = new java.util.HashSet<>();

        for (Shipment collect : collects) {
            List<Shipment> deliveries = shipmentRepository.findByOperationOrigem(collect);

            List<String> collectVisitTypes = new ArrayList<>();
            if (collect.getTypeTransport() != null && collect.getTypeTransport().getName() != null) {
                collectVisitTypes.add(collect.getTypeTransport().getName().trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_"));
            }
            if (collect.getVisitTypes() != null) {
                collectVisitTypes.addAll(collect.getVisitTypes().stream().map(VisitType::getCode).toList());
            }

            String cLat = (collect.getAddress() != null && collect.getAddress().getLatitude() != null && !collect.getAddress().getLatitude().isBlank())
                    ? collect.getAddress().getLatitude() : "-23.550520";
            String cLng = (collect.getAddress() != null && collect.getAddress().getLongitude() != null && !collect.getAddress().getLongitude().isBlank())
                    ? collect.getAddress().getLongitude() : "-46.633308";

            LatLng pickupLocation = LatLng.newBuilder()
                    .setLatitude(Double.parseDouble(cLat))
                    .setLongitude(Double.parseDouble(cLng))
                    .build();

            Instant collectInstant = collect.getSchedulind().atOffset(offset).toInstant();
            Instant collectMinusLead = collect.getSchedulind().minusMinutes(settings.timeWindowLeadMinutes()).atOffset(offset).toInstant();

            TimeWindow pickupWindow = TimeWindow.newBuilder()
                    .setStartTime(Timestamp.newBuilder().setSeconds(collectMinusLead.getEpochSecond()).setNanos(collectMinusLead.getNano()))
                    .setEndTime(Timestamp.newBuilder().setSeconds(collectInstant.getEpochSecond()).setNanos(collectInstant.getNano()))
                    .build();

            if (!deliveries.isEmpty()) {
                for (Shipment delivery : deliveries) {
                    handledDeliveryIds.add(delivery.getId());

                    List<String> deliveryVisitTypes = new ArrayList<>();
                    if (delivery.getTypeTransport() != null && delivery.getTypeTransport().getName() != null) {
                        deliveryVisitTypes.add(delivery.getTypeTransport().getName().trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_"));
                    }
                    if (delivery.getVisitTypes() != null) {
                        deliveryVisitTypes.addAll(delivery.getVisitTypes().stream().map(VisitType::getCode).toList());
                    }

                    String dLat = (delivery.getAddress() != null && delivery.getAddress().getLatitude() != null && !delivery.getAddress().getLatitude().isBlank())
                            ? delivery.getAddress().getLatitude() : "-23.550520";
                    String dLng = (delivery.getAddress() != null && delivery.getAddress().getLongitude() != null && !delivery.getAddress().getLongitude().isBlank())
                            ? delivery.getAddress().getLongitude() : "-46.633308";

                    LatLng deliveryLocation = LatLng.newBuilder()
                            .setLatitude(Double.parseDouble(dLat))
                            .setLongitude(Double.parseDouble(dLng))
                            .build();

                    Instant deliveryInstant = delivery.getSchedulind().atOffset(offset).toInstant();
                    Instant deliveryMinusLead = delivery.getSchedulind().minusMinutes(settings.timeWindowLeadMinutes()).atOffset(offset).toInstant();

                    TimeWindow deliveryWindow = TimeWindow.newBuilder()
                            .setStartTime(Timestamp.newBuilder().setSeconds(deliveryMinusLead.getEpochSecond()).setNanos(deliveryMinusLead.getNano()))
                            .setEndTime(Timestamp.newBuilder().setSeconds(deliveryInstant.getEpochSecond()).setNanos(deliveryInstant.getNano()))
                            .build();

                    var deliveryDemands = configResolver.resolveShipmentLoadDemands(delivery);
                    Map<String, com.google.maps.routeoptimization.v1.Shipment.Load> protoDemands = new HashMap<>();
                    for (var demandEntry : deliveryDemands.entrySet()) {
                        long amount = demandEntry.getValue().amount() != null ? Long.parseLong(demandEntry.getValue().amount()) : 0L;
                        protoDemands.put(demandEntry.getKey(), com.google.maps.routeoptimization.v1.Shipment.Load.newBuilder()
                                .setAmount(amount)
                                .build());
                    }

                    com.google.maps.routeoptimization.v1.Shipment.VisitRequest.Builder pickupReqBuilder = com.google.maps.routeoptimization.v1.Shipment.VisitRequest.newBuilder()
                            .setArrivalLocation(pickupLocation)
                            .setDuration(stopDuration)
                            .addTimeWindows(pickupWindow)
                            .putAllLoadDemands(protoDemands);
                    if (!collectVisitTypes.isEmpty()) {
                        pickupReqBuilder.addAllVisitTypes(collectVisitTypes);
                    }

                    com.google.maps.routeoptimization.v1.Shipment.VisitRequest.Builder deliveryReqBuilder = com.google.maps.routeoptimization.v1.Shipment.VisitRequest.newBuilder()
                            .setArrivalLocation(deliveryLocation)
                            .setDuration(stopDuration)
                            .addTimeWindows(deliveryWindow)
                            .putAllLoadDemands(protoDemands);
                    if (!deliveryVisitTypes.isEmpty()) {
                        deliveryReqBuilder.addAllVisitTypes(deliveryVisitTypes);
                    }

                    com.google.maps.routeoptimization.v1.Shipment protoShipment = com.google.maps.routeoptimization.v1.Shipment.newBuilder()
                            .setLabel(collect.getId().toString() + "/" + delivery.getId().toString())
                            .addPickups(pickupReqBuilder.build())
                            .addDeliveries(deliveryReqBuilder.build())
                            .setPenaltyCost(settings.penaltyCostUnserved())
                            .build();

                    shipmentModelBuilder.addShipments(protoShipment);
                }
            } else {
                // Coleta individual sem entrega filha cadastrada ainda
                var collectDemands = configResolver.resolveShipmentLoadDemands(collect);
                Map<String, com.google.maps.routeoptimization.v1.Shipment.Load> protoDemands = new HashMap<>();
                for (var demandEntry : collectDemands.entrySet()) {
                    long amount = demandEntry.getValue().amount() != null ? Long.parseLong(demandEntry.getValue().amount()) : 0L;
                    protoDemands.put(demandEntry.getKey(), com.google.maps.routeoptimization.v1.Shipment.Load.newBuilder()
                            .setAmount(amount)
                            .build());
                }

                com.google.maps.routeoptimization.v1.Shipment.VisitRequest.Builder pickupReqBuilder = com.google.maps.routeoptimization.v1.Shipment.VisitRequest.newBuilder()
                        .setArrivalLocation(pickupLocation)
                        .setDuration(stopDuration)
                        .addTimeWindows(pickupWindow)
                        .putAllLoadDemands(protoDemands);
                if (!collectVisitTypes.isEmpty()) {
                    pickupReqBuilder.addAllVisitTypes(collectVisitTypes);
                }

                com.google.maps.routeoptimization.v1.Shipment protoShipment = com.google.maps.routeoptimization.v1.Shipment.newBuilder()
                        .setLabel("pickup/" + collect.getId().toString())
                        .addPickups(pickupReqBuilder.build())
                        .setPenaltyCost(settings.penaltyCostUnserved())
                        .build();

                shipmentModelBuilder.addShipments(protoShipment);
            }
        }

        // Processa Entregas Avulsas (ex: importadas via NF-e ERP ou criadas diretamente)
        List<Shipment> standaloneDeliveries = requestedShipments.stream()
                .filter(s -> s.getTypeOperation() == TypeOperation.ENTREGA && !handledDeliveryIds.contains(s.getId()))
                .toList();

        for (Shipment delivery : standaloneDeliveries) {
            List<String> deliveryVisitTypes = new ArrayList<>();
            if (delivery.getTypeTransport() != null && delivery.getTypeTransport().getName() != null) {
                deliveryVisitTypes.add(delivery.getTypeTransport().getName().trim().toUpperCase().replaceAll("[^A-Z0-9_]", "_"));
            }
            if (delivery.getVisitTypes() != null) {
                deliveryVisitTypes.addAll(delivery.getVisitTypes().stream().map(VisitType::getCode).toList());
            }

            String dLat = (delivery.getAddress() != null && delivery.getAddress().getLatitude() != null && !delivery.getAddress().getLatitude().isBlank())
                    ? delivery.getAddress().getLatitude() : "-23.550520";
            String dLng = (delivery.getAddress() != null && delivery.getAddress().getLongitude() != null && !delivery.getAddress().getLongitude().isBlank())
                    ? delivery.getAddress().getLongitude() : "-46.633308";

            LatLng deliveryLocation = LatLng.newBuilder()
                    .setLatitude(Double.parseDouble(dLat))
                    .setLongitude(Double.parseDouble(dLng))
                    .build();

            Instant deliveryInstant = delivery.getSchedulind().atOffset(offset).toInstant();
            Instant deliveryMinusLead = delivery.getSchedulind().minusMinutes(settings.timeWindowLeadMinutes()).atOffset(offset).toInstant();

            TimeWindow deliveryWindow = TimeWindow.newBuilder()
                    .setStartTime(Timestamp.newBuilder().setSeconds(deliveryMinusLead.getEpochSecond()).setNanos(deliveryMinusLead.getNano()))
                    .setEndTime(Timestamp.newBuilder().setSeconds(deliveryInstant.getEpochSecond()).setNanos(deliveryInstant.getNano()))
                    .build();

            var deliveryDemands = configResolver.resolveShipmentLoadDemands(delivery);
            Map<String, com.google.maps.routeoptimization.v1.Shipment.Load> protoDemands = new HashMap<>();
            for (var demandEntry : deliveryDemands.entrySet()) {
                long amount = demandEntry.getValue().amount() != null ? Long.parseLong(demandEntry.getValue().amount()) : 0L;
                protoDemands.put(demandEntry.getKey(), com.google.maps.routeoptimization.v1.Shipment.Load.newBuilder()
                        .setAmount(amount)
                        .build());
            }

            com.google.maps.routeoptimization.v1.Shipment.VisitRequest.Builder deliveryReqBuilder = com.google.maps.routeoptimization.v1.Shipment.VisitRequest.newBuilder()
                    .setArrivalLocation(deliveryLocation)
                    .setDuration(stopDuration)
                    .addTimeWindows(deliveryWindow)
                    .putAllLoadDemands(protoDemands);
            if (!deliveryVisitTypes.isEmpty()) {
                deliveryReqBuilder.addAllVisitTypes(deliveryVisitTypes);
            }

            com.google.maps.routeoptimization.v1.Shipment.Builder shipmentBuilder = com.google.maps.routeoptimization.v1.Shipment.newBuilder()
                    .setPenaltyCost(settings.penaltyCostUnserved())
                    .addDeliveries(deliveryReqBuilder.build());

            if (delivery.getOperationOrigem() != null) {
                Shipment origCollect = delivery.getOperationOrigem();
                String ocLat = (origCollect.getAddress() != null && origCollect.getAddress().getLatitude() != null && !origCollect.getAddress().getLatitude().isBlank())
                        ? origCollect.getAddress().getLatitude() : "-23.550520";
                String ocLng = (origCollect.getAddress() != null && origCollect.getAddress().getLongitude() != null && !origCollect.getAddress().getLongitude().isBlank())
                        ? origCollect.getAddress().getLongitude() : "-46.633308";

                LatLng ocLocation = LatLng.newBuilder()
                        .setLatitude(Double.parseDouble(ocLat))
                        .setLongitude(Double.parseDouble(ocLng))
                        .build();

                Instant ocInstant = origCollect.getSchedulind().atOffset(offset).toInstant();
                Instant ocMinusLead = origCollect.getSchedulind().minusMinutes(settings.timeWindowLeadMinutes()).atOffset(offset).toInstant();

                TimeWindow ocWindow = TimeWindow.newBuilder()
                        .setStartTime(Timestamp.newBuilder().setSeconds(ocMinusLead.getEpochSecond()).setNanos(ocMinusLead.getNano()))
                        .setEndTime(Timestamp.newBuilder().setSeconds(ocInstant.getEpochSecond()).setNanos(ocInstant.getNano()))
                        .build();

                com.google.maps.routeoptimization.v1.Shipment.VisitRequest.Builder origPickupBuilder = com.google.maps.routeoptimization.v1.Shipment.VisitRequest.newBuilder()
                        .setArrivalLocation(ocLocation)
                        .setDuration(stopDuration)
                        .addTimeWindows(ocWindow)
                        .putAllLoadDemands(protoDemands);

                shipmentBuilder.addPickups(origPickupBuilder.build());
                shipmentBuilder.setLabel(origCollect.getId().toString() + "/" + delivery.getId().toString());
            } else {
                shipmentBuilder.setLabel("delivery/" + delivery.getId().toString());
            }

            shipmentModelBuilder.addShipments(shipmentBuilder.build());
        }

        // Horizonte Global Dinâmico: engloba todas as datas de escalas, remessas e folga operacional
        LocalDate minDate = LocalDate.now();
        LocalDate maxDate = LocalDate.now();

        for (WorkSchedule workSchedule : workSchedules) {
            if (workSchedule.getScheduleDate() != null) {
                if (workSchedule.getScheduleDate().isBefore(minDate)) minDate = workSchedule.getScheduleDate();
                if (workSchedule.getScheduleDate().isAfter(maxDate)) maxDate = workSchedule.getScheduleDate();
            }
        }
        for (Shipment s : requestedShipments) {
            if (s.getSchedulind() != null) {
                LocalDate sDate = s.getSchedulind().toLocalDate();
                if (sDate.isBefore(minDate)) minDate = sDate;
                if (sDate.isAfter(maxDate)) maxDate = sDate;
            }
            if (s.getOperationOrigem() != null && s.getOperationOrigem().getSchedulind() != null) {
                LocalDate oDate = s.getOperationOrigem().getSchedulind().toLocalDate();
                if (oDate.isBefore(minDate)) minDate = oDate;
                if (oDate.isAfter(maxDate)) maxDate = oDate;
            }
        }

        int extraDays = settings.globalHorizonExtraDays();
        Instant globalStartInstant = minDate.minusDays(1).atStartOfDay().atOffset(offset).toInstant();
        Instant globalEndInstant = maxDate.plusDays(extraDays).atTime(23, 59, 59).atOffset(offset).toInstant();

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
                    shipmentModelBuilder.addShipmentTypeIncompatibilities(com.google.maps.routeoptimization.v1.ShipmentTypeIncompatibility.newBuilder()
                            .addTypes(rule.getVisitType1().getCode())
                            .addTypes(rule.getVisitType2().getCode())
                            .setIncompatibilityMode(com.google.maps.routeoptimization.v1.ShipmentTypeIncompatibility.IncompatibilityMode.NOT_PERFORMED_BY_SAME_VEHICLE)
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
