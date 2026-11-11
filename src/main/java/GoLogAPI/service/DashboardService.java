package GoLogAPI.service;

import GoLogAPI.dto.dashboard.DashboardMetricsResponse;
import GoLogAPI.dto.dashboard.DashboardMetricsResponse.*;
import GoLogAPI.infra.tenant.TenantGuard;
import GoLogAPI.model.*;
import GoLogAPI.model.enums.EquipamentStatus;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.model.enums.TypeOperation;
import GoLogAPI.repository.*;
import GoLogAPI.service.sustainability.EmissionFactors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Indicadores do painel, calculados exclusivamente sobre dados registrados.
 *
 * <p>Princípios (o painel precisa ser defensável em banca/auditoria):</p>
 * <ol>
 *   <li>Nada é presumido: sem dado, o indicador vem {@code null} e a tela mostra "sem dados".</li>
 *   <li>Toda comparação declara a linha de base (ver {@link #sequencing}).</li>
 *   <li>A fórmula de cada indicador é devolvida em {@code methodology} e exibida como dica.</li>
 * </ol>
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    private final DriverRepository driverRepository;
    private final TransportRepository transportRepository;
    private final ShipmentRepository shipmentRepository;
    private final RouteStopRepository routeStopRepository;
    private final EquipamentRepository equipamentRepository;
    private final OccurrenceRepository occurrenceRepository;
    private final TrailerRepository trailerRepository;
    private final TenantGuard tenantGuard;

    public DashboardService(DriverRepository driverRepository, TransportRepository transportRepository,
                            ShipmentRepository shipmentRepository, RouteStopRepository routeStopRepository,
                            EquipamentRepository equipamentRepository, OccurrenceRepository occurrenceRepository,
                            TrailerRepository trailerRepository, TenantGuard tenantGuard) {
        this.driverRepository = driverRepository;
        this.transportRepository = transportRepository;
        this.shipmentRepository = shipmentRepository;
        this.routeStopRepository = routeStopRepository;
        this.equipamentRepository = equipamentRepository;
        this.occurrenceRepository = occurrenceRepository;
        this.trailerRepository = trailerRepository;
        this.tenantGuard = tenantGuard;
    }

    public DashboardMetricsResponse getMetrics() {
        boolean global = tenantGuard.isGlobalView();
        UUID scope = tenantGuard.scope();
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();

        List<Driver> drivers = global ? driverRepository.findAll() : driverRepository.findByCompanyId(scope);
        List<Equipament> equipaments = global ? equipamentRepository.findAll() : equipamentRepository.findByCompanyId(scope);
        List<Shipment> shipments = global ? shipmentRepository.findAll() : shipmentRepository.findByCompanyId(scope);
        List<Transport> transports = global ? transportRepository.findAll() : transportRepository.findByTransporterId(scope);
        Set<UUID> transportIds = transports.stream().map(Transport::getId).collect(Collectors.toSet());

        Map<UUID, List<RouteStop>> stopsByTransport = routeStopRepository.findAll().stream()
                .filter(rs -> rs.getTransport() != null && transportIds.contains(rs.getTransport().getId()))
                .collect(Collectors.groupingBy(rs -> rs.getTransport().getId()));
        stopsByTransport.values().forEach(l -> l.sort(Comparator.comparing(RouteStop::getSequenceOrder)));

        Map<UUID, LocalDateTime> completionByShipment = completionTimes(shipments, stopsByTransport);

        // ------------------------------------------------------------- Operação
        List<Shipment> deliveries = shipments.stream().filter(s -> s.getTypeOperation() == TypeOperation.ENTREGA).toList();
        long pending = countStatus(shipments, ShipmentStatus.PENDENTE);
        long planned = countStatus(shipments, ShipmentStatus.AGUARDANDO_INICIO);
        long inProgress = countStatus(shipments, ShipmentStatus.INICIADO);
        long delivered = countStatus(shipments, ShipmentStatus.FINALIZADO);
        long overdue = shipments.stream()
                .filter(s -> s.getStatus() != ShipmentStatus.FINALIZADO && s.getSchedulind() != null && s.getSchedulind().isBefore(now))
                .count();
        List<Shipment> todays = shipments.stream()
                .filter(s -> s.getSchedulind() != null && s.getSchedulind().toLocalDate().equals(today)).toList();

        long tripsPlanned = 0, tripsInProgress = 0, tripsFinished = 0;
        for (Transport t : transports) {
            List<RouteStop> stops = stopsByTransport.getOrDefault(t.getId(), List.of());
            if (stops.isEmpty()) continue;
            long done = stops.stream().filter(rs -> rs.getShipment() != null && rs.getShipment().getStatus() == ShipmentStatus.FINALIZADO).count();
            long started = stops.stream().filter(rs -> rs.getShipment() != null
                    && (rs.getShipment().getStatus() == ShipmentStatus.INICIADO || rs.getShipment().getStatus() == ShipmentStatus.FINALIZADO)).count();
            if (done == stops.size()) tripsFinished++;
            else if (started > 0) tripsInProgress++;
            else tripsPlanned++;
        }

        Operation operation = new Operation(
                drivers.size(),
                equipaments.stream().filter(e -> e.getStatus() == EquipamentStatus.ATIVO).count(),
                pending, planned, inProgress, delivered, overdue,
                todays.size(), todays.stream().filter(s -> s.getStatus() == ShipmentStatus.FINALIZADO).count(),
                tripsPlanned, tripsInProgress, tripsFinished);

        // ------------------------------------------------------------- SLA
        long withTs = 0, onTime = 0, withoutTs = 0;
        for (Shipment s : deliveries) {
            if (s.getStatus() != ShipmentStatus.FINALIZADO) continue;
            LocalDateTime done = completionByShipment.get(s.getId());
            if (done == null || s.getSchedulind() == null) { withoutTs++; continue; }
            withTs++;
            if (!done.isAfter(s.getSchedulind())) onTime++;
        }
        Sla sla = new Sla(withTs, onTime, withTs == 0 ? null : round(100.0 * onTime / withTs, 1), withoutTs);

        // ------------------------------------------------------------- Planejamento, emissões e sequenciamento
        double plannedKm = 0, plannedHours = 0, plannedCost = 0, tonKm = 0, realizedKm = 0;
        long plannedStops = 0, tripsWithStops = 0, tripsWithCapacity = 0, tripsWithFactor = 0, tripsWithRealized = 0;
        double loadFactorSum = 0, co2Planned = 0, co2Realized = 0, tonKmWithFactor = 0;
        long deliveriesWithFactor = 0;
        long seqTrips = 0;
        double seqOptimized = 0, seqBaseline = 0;
        Map<LocalDate, double[]> dailyKm = new TreeMap<>();      // [km, co2, co2Known(0/1)]

        for (Transport t : transports) {
            List<RouteStop> stops = stopsByTransport.getOrDefault(t.getId(), List.of());
            if (stops.isEmpty()) continue;
            tripsWithStops++;
            plannedStops += stops.size();
            double km = nz(t.getCalculedDistance()) / 1000.0;
            plannedKm += km;
            plannedHours += nz(t.getTotalTimeCalculed()) / 3600.0;
            plannedCost += t.getTotalCostCalculed() != null ? t.getTotalCostCalculed() : 0.0;

            double tripTonKm = tonKm(stops);
            tonKm += tripTonKm;

            Double capacityKg = capacityKg(t.getEquipamentGroup());
            double deliveredKg = stops.stream().map(RouteStop::getShipment).filter(Objects::nonNull)
                    .filter(s -> s.getTypeOperation() == TypeOperation.ENTREGA).mapToDouble(s -> nz(s.getWeight())).sum();
            if (capacityKg != null && capacityKg > 0) {
                tripsWithCapacity++;
                loadFactorSum += Math.min(deliveredKg / capacityKg, 1.0);
            }

            Double factor = co2Factor(t);
            long tripDeliveries = stops.stream().filter(rs -> rs.getShipment() != null
                    && rs.getShipment().getTypeOperation() == TypeOperation.ENTREGA).count();
            if (factor != null) {
                tripsWithFactor++;
                co2Planned += km * factor;
                tonKmWithFactor += tripTonKm;
                deliveriesWithFactor += tripDeliveries;
            }
            if (t.getDistanceTraveled() != null && t.getDistanceTraveled() > 0) {
                tripsWithRealized++;
                realizedKm += t.getDistanceTraveled() / 1000.0;
                if (factor != null) co2Realized += (t.getDistanceTraveled() / 1000.0) * factor;
            }

            double[] seq = sequencing(t, stops);
            if (seq != null) {
                seqTrips++;
                seqOptimized += seq[0];
                seqBaseline += seq[1];
            }

            LocalDate day = stops.stream().map(RouteStop::getShipment).filter(Objects::nonNull)
                    .map(Shipment::getSchedulind).filter(Objects::nonNull).min(Comparator.naturalOrder())
                    .map(LocalDateTime::toLocalDate).orElse(null);
            if (day != null) {
                double[] agg = dailyKm.computeIfAbsent(day, d -> new double[]{0, 0, 1});
                agg[0] += km;
                if (factor != null) agg[1] += km * factor; else agg[2] = 0;
            }
        }

        Planning planning = new Planning(tripsWithStops, plannedStops, round(plannedKm, 1), round(plannedHours, 1),
                round(plannedCost, 2), tripsWithStops == 0 ? null : round((double) plannedStops / tripsWithStops, 1),
                tripsWithCapacity == 0 ? null : round(100.0 * loadFactorSum / tripsWithCapacity, 1),
                tripsWithCapacity, round(tonKm, 1));

        Emissions emissions = new Emissions(
                tripsWithFactor == 0 ? null : round(co2Planned, 1),
                tripsWithRealized == 0 || tripsWithFactor == 0 ? null : round(co2Realized, 1),
                tripsWithFactor, tripsWithStops,
                deliveriesWithFactor == 0 ? null : round(co2Planned / deliveriesWithFactor, 2),
                tonKmWithFactor <= 0 ? null : round(co2Planned * 1000.0 / tonKmWithFactor, 1),
                round(realizedKm, 1), tripsWithRealized);

        Sequencing sequencing = new Sequencing(seqTrips, round(seqOptimized, 1), round(seqBaseline, 1),
                round(seqBaseline - seqOptimized, 1),
                seqBaseline <= 0 ? null : round(100.0 * (seqBaseline - seqOptimized) / seqBaseline, 1));

        // ------------------------------------------------------------- Série diária (7 dias antes / 14 depois)
        List<DailyPoint> daily = new ArrayList<>();
        for (LocalDate d = today.minusDays(7); !d.isAfter(today.plusDays(14)); d = d.plusDays(1)) {
            final LocalDate day = d;
            List<Shipment> ofDay = shipments.stream()
                    .filter(s -> s.getSchedulind() != null && s.getSchedulind().toLocalDate().equals(day)).toList();
            double[] agg = dailyKm.get(day);
            if (ofDay.isEmpty() && agg == null) continue;
            daily.add(new DailyPoint(day, ofDay.size(),
                    ofDay.stream().filter(s -> s.getStatus() == ShipmentStatus.FINALIZADO).count(),
                    agg == null ? 0.0 : round(agg[0], 1),
                    agg == null || agg[2] == 0 ? null : round(agg[1], 1)));
        }

        return new DashboardMetricsResponse(now, operation, sla, planning, emissions, sequencing, daily, methodology());
    }

    // ================================================================= cálculos

    /** Conclusão = saída da parada (geofence) ou, na falta dela, a ocorrência "Fim" registrada pelo motorista. */
    private Map<UUID, LocalDateTime> completionTimes(List<Shipment> shipments, Map<UUID, List<RouteStop>> stopsByTransport) {
        Set<UUID> ids = shipments.stream().map(Shipment::getId).collect(Collectors.toSet());
        Map<UUID, LocalDateTime> result = new HashMap<>();
        stopsByTransport.values().stream().flatMap(List::stream)
                .filter(rs -> rs.getShipment() != null && rs.getDepartedAt() != null)
                .forEach(rs -> result.put(rs.getShipment().getId(), rs.getDepartedAt()));
        for (Occurrence o : occurrenceRepository.findAll()) {
            if (o.getShipment() == null || !ids.contains(o.getShipment().getId()) || o.getType() == null) continue;
            if (!o.getType().trim().equalsIgnoreCase("Fim") || o.getDateTime() == null) continue;
            result.merge(o.getShipment().getId(), o.getDateTime(), (a, b) -> a);
        }
        return result;
    }

    /**
     * Toneladas transportadas × km de cada trecho. A carga a bordo é reconstruída pela sequência: entregas sem
     * coleta na própria viagem saem carregadas do ponto de partida; coletas adicionam o peso das entregas
     * vinculadas (ou o próprio peso, se não houver entrega vinculada na viagem); entregas descarregam.
     */
    static double tonKm(List<RouteStop> stops) {
        Set<UUID> collectsInTrip = stops.stream().map(RouteStop::getShipment).filter(Objects::nonNull)
                .filter(s -> s.getTypeOperation() == TypeOperation.COLETA).map(Shipment::getId).collect(Collectors.toSet());
        Map<UUID, Double> childWeightByCollect = new HashMap<>();
        double onboardKg = 0;
        for (RouteStop rs : stops) {
            Shipment s = rs.getShipment();
            if (s == null || s.getTypeOperation() != TypeOperation.ENTREGA) continue;
            if (s.getOperationOrigem() != null && collectsInTrip.contains(s.getOperationOrigem().getId())) {
                childWeightByCollect.merge(s.getOperationOrigem().getId(), nz(s.getWeight()), Double::sum);
            } else {
                onboardKg += nz(s.getWeight());
            }
        }
        double total = 0;
        for (RouteStop rs : stops) {
            total += (onboardKg / 1000.0) * (nz(rs.getCalculatedDistance()) / 1000.0);
            Shipment s = rs.getShipment();
            if (s == null) continue;
            if (s.getTypeOperation() == TypeOperation.ENTREGA) {
                onboardKg = Math.max(0, onboardKg - nz(s.getWeight()));
            } else {
                onboardKg += childWeightByCollect.getOrDefault(s.getId(), nz(s.getWeight()));
            }
        }
        return total;
    }

    /** Capacidade de peso do conjunto: soma das carretas quando houver, senão a do próprio veículo. */
    private Double capacityKg(EquipamentGroup group) {
        if (group == null) return null;
        double trailers = 0;
        for (Equipament e : new Equipament[]{group.getEquipament2(), group.getEquipament3()}) {
            if (e != null && e.getMaximumCapacity() != null) trailers += e.getMaximumCapacity();
        }
        if (trailers > 0) return trailers;
        return group.getEquipament1() != null ? group.getEquipament1().getMaximumCapacity() : null;
    }

    private Double co2Factor(Transport t) {
        EquipamentGroup g = t.getEquipamentGroup();
        if (g == null || !(g.getEquipament1() instanceof Tractor tractor)) return null;
        return EmissionFactors.kgCo2PerKm(tractor);
    }

    /**
     * [km otimizado, km linha de base] em linha reta, ambos partindo do endereço do tenant transportador e
     * passando pelas mesmas paradas: na ordem do otimizador vs. na ordem do horário agendado. Retorno ao CD
     * excluído dos dois lados. Requer ≥ 2 paradas com coordenadas válidas.
     */
    private double[] sequencing(Transport t, List<RouteStop> stops) {
        double[] depot = t.getTransporter() != null ? coords(t.getTransporter().getAddress()) : null;
        if (depot == null || stops.size() < 2) return null;
        List<double[]> optimized = new ArrayList<>();
        List<RouteStop> chrono = new ArrayList<>(stops);
        chrono.sort(Comparator.comparing((RouteStop rs) -> rs.getShipment() != null && rs.getShipment().getSchedulind() != null
                        ? rs.getShipment().getSchedulind() : LocalDateTime.MAX)
                .thenComparing(rs -> rs.getShipment() != null && rs.getShipment().getTypeOperation() == TypeOperation.COLETA ? 0 : 1));
        for (RouteStop rs : stops) {
            double[] c = rs.getShipment() != null ? coords(rs.getShipment().getAddress()) : null;
            if (c == null) return null;
            optimized.add(c);
        }
        List<double[]> baseline = new ArrayList<>();
        for (RouteStop rs : chrono) baseline.add(coords(rs.getShipment().getAddress()));
        return new double[]{pathKm(depot, optimized), pathKm(depot, baseline)};
    }

    private static double pathKm(double[] start, List<double[]> points) {
        double total = 0;
        double[] prev = start;
        for (double[] p : points) {
            total += haversineKm(prev, p);
            prev = p;
        }
        return total;
    }

    static double haversineKm(double[] a, double[] b) {
        double dLat = Math.toRadians(b[0] - a[0]);
        double dLon = Math.toRadians(b[1] - a[1]);
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(a[0])) * Math.cos(Math.toRadians(b[0])) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(h));
    }

    private static double[] coords(Address a) {
        if (a == null || a.getLatitude() == null || a.getLongitude() == null) return null;
        try {
            return new double[]{Double.parseDouble(a.getLatitude().trim().replace(',', '.')),
                    Double.parseDouble(a.getLongitude().trim().replace(',', '.'))};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static long countStatus(List<Shipment> shipments, ShipmentStatus status) {
        return shipments.stream().filter(s -> s.getStatus() == status).count();
    }

    private static double nz(Number n) {
        return n == null ? 0.0 : n.doubleValue();
    }

    private static double round(double v, int places) {
        double f = Math.pow(10, places);
        return Math.round(v * f) / f;
    }

    private static Map<String, String> methodology() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("sla", "Entregas concluídas até o horário agendado ÷ entregas concluídas com horário de conclusão registrado "
                + "(saída da parada por geofence ou ocorrência \"Fim\" do motorista). Entregas sem registro de horário ficam fora da conta.");
        m.put("overdue", "Remessas com horário agendado no passado que ainda não foram finalizadas.");
        m.put("plannedKm", "Soma das distâncias rodoviárias devolvidas pelo Google Route Optimization para as viagens planejadas.");
        m.put("loadFactor", "Peso das entregas da viagem ÷ capacidade de peso do conjunto (carretas ou, sem carreta, o próprio veículo). "
                + "Média das viagens cujo conjunto tem capacidade cadastrada.");
        m.put("tonKm", "Σ (toneladas a bordo em cada trecho × km do trecho), com a carga reconstruída pela sequência de coletas e entregas.");
        m.put("co2", "km da viagem × fator do veículo. Fator = kg CO₂/km informado no cadastro ou, na falta dele, fator do combustível "
                + "(IPCC 2006: diesel 2,68 kg/L; gasolina 2,31 kg/L; etanol e elétrico 0 em escopo 1) ÷ consumo médio (km/L) cadastrado. "
                + "Veículos sem consumo informado ficam fora do total — a cobertura é exibida.");
        m.put("co2Realized", "Mesma fórmula aplicada à distância realmente percorrida, quando registrada na conclusão da viagem.");
        m.put("sequencing", "Linha de base: as mesmas paradas, no mesmo veículo, partindo do endereço da transportadora, atendidas na ordem "
                + "do horário agendado. Comparação feita em linha reta (haversine) nos dois lados, sem retorno ao CD. "
                + "É uma estimativa do ganho de sequenciamento, não uma medição rodoviária.");
        return m;
    }
}
