package GoLogAPI.service;

import GoLogAPI.dto.transport.TripIndicatorsResponse;
import GoLogAPI.dto.transport.TripIndicatorsResponse.CostBreakdown;
import GoLogAPI.dto.transport.TripIndicatorsResponse.ScheduleAdherence;
import GoLogAPI.dto.transport.TripIndicatorsResponse.VehicleFactors;
import GoLogAPI.model.EquipamentGroup;
import GoLogAPI.model.RouteStop;
import GoLogAPI.model.Shipment;
import GoLogAPI.model.Tractor;
import GoLogAPI.model.Transport;
import GoLogAPI.model.enums.TypeFuel;
import GoLogAPI.repository.RouteStopRepository;
import GoLogAPI.service.sustainability.EmissionFactors;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Indicadores realizados da viagem (acompanhamento e resumo operacional). A base — distância e t·km — é
 * acumulada na ingestão de telemetria ({@code TripTrackingService}); aqui só se aplicam os fatores do cadastro,
 * com as mesmas regras do dashboard ({@link EmissionFactors}).
 */
@Service
public class TripIndicatorsService {

    private final RouteStopRepository routeStopRepository;
    private final TransportService transportService;

    public TripIndicatorsService(RouteStopRepository routeStopRepository, TransportService transportService) {
        this.routeStopRepository = routeStopRepository;
        this.transportService = transportService;
    }

    /** Indicadores de uma viagem visível para o tenant atual. */
    @Transactional(readOnly = true)
    public TripIndicatorsResponse forTransport(UUID transportId) {
        return compute(transportService.findVisible(transportId));
    }

    public TripIndicatorsResponse compute(Transport t) {
        List<RouteStop> stops = routeStopRepository.findByTransportIdOrderBySequenceOrderAsc(t.getId());
        Tractor tractor = tractor(t);

        Double kmPerLiter = tractor != null ? tractor.getKmPerLiter() : null;
        Double kgCo2PerKm = EmissionFactors.kgCo2PerKm(tractor);
        Double costPerKm = costPerKm(t, tractor);
        Double costPerHour = plannedHourlyRate(t);
        VehicleFactors factors = new VehicleFactors(kmPerLiter, round(kgCo2PerKm, 4), round(costPerKm, 2), round(costPerHour, 2));

        ScheduleAdherence schedule = schedule(stops, LocalDateTime.now());

        double km = t.getDistanceTraveled() == null ? 0.0 : t.getDistanceTraveled() / 1000.0;
        boolean hasRoute = t.getTrackingStartedAt() != null && km > 0;
        if (!hasRoute) {
            return new TripIndicatorsResponse(t.getId(), false, t.getTrackingStartedAt(), t.getLastTelemetryAt(),
                    0.0, 0.0, 0.0, 0.0, 0.0, 0.0, null, new CostBreakdown(0, 0, 0), schedule, factors);
        }

        double hours = Duration.between(t.getTrackingStartedAt(), t.getLastTelemetryAt()).toSeconds() / 3600.0;
        double tonKm = t.getTonKmTraveled() == null ? 0.0 : t.getTonKmTraveled();

        Double fuel = fuelLiters(tractor, km);
        Double co2 = kgCo2PerKm == null ? null : km * kgCo2PerKm;

        Double effectiveCost = null;
        CostBreakdown cost = null;
        if (costPerKm != null) {
            double kmCost = km * costPerKm;
            double hourCost = costPerHour == null ? 0.0 : hours * costPerHour;
            double fixed = plannedFixedCost(t);
            cost = new CostBreakdown(round(kmCost, 2), round(hourCost, 2), round(fixed, 2));
            effectiveCost = kmCost + hourCost + fixed;
        }

        return new TripIndicatorsResponse(t.getId(), true, t.getTrackingStartedAt(), t.getLastTelemetryAt(),
                round(km, 2), round(hours, 2), round(tonKm, 2), round(fuel, 2), round(co2, 2),
                round(effectiveCost, 2), effectiveCost == null ? null : round(effectiveCost / km, 2),
                cost, schedule, factors);
    }

    // ================================================================= regras

    /** Litros = km ÷ consumo médio; elétrico não consome combustível; sem km/L cadastrado não há valor. */
    static Double fuelLiters(Tractor tractor, double km) {
        if (tractor == null) return null;
        if (tractor.getTypeFuel() == TypeFuel.ELETRICO) return 0.0;
        Double kmPerLiter = tractor.getKmPerLiter();
        return kmPerLiter == null || kmPerLiter <= 0 ? null : km / kmPerLiter;
    }

    /** Agenda atendida = chegada (geofence ou registro manual) até o horário agendado da remessa. */
    static ScheduleAdherence schedule(List<RouteStop> stops, LocalDateTime now) {
        int scheduled = 0, onTime = 0, late = 0, pending = 0;
        for (RouteStop rs : stops) {
            Shipment s = rs.getShipment();
            if (s == null || s.getSchedulind() == null) continue;
            scheduled++;
            LocalDateTime arrived = rs.getArrivedAt() != null ? rs.getArrivedAt() : rs.getDepartedAt();
            if (arrived != null) {
                if (arrived.isAfter(s.getSchedulind())) late++; else onTime++;
            } else if (now.isAfter(s.getSchedulind())) {
                late++;
            } else {
                pending++;
            }
        }
        int resolved = onTime + late;
        return new ScheduleAdherence(scheduled, onTime, late, pending,
                resolved == 0 ? null : round(100.0 * onTime / resolved, 1));
    }

    private static Tractor tractor(Transport t) {
        EquipamentGroup g = t.getEquipamentGroup();
        if (g == null || g.getEquipament1() == null) return null;
        Object e = Hibernate.unproxy(g.getEquipament1());
        return e instanceof Tractor tractor ? tractor : null;
    }

    /** Custo/km do cadastro do veículo; sem ele, o custo/km implícito no plano do otimizador. */
    private static Double costPerKm(Transport t, Tractor tractor) {
        if (tractor != null && tractor.getCostPerKilometer() != null) return tractor.getCostPerKilometer();
        if (t.getCostKmCalculed() != null && t.getCalculedDistance() != null && t.getCalculedDistance() > 0) {
            return t.getCostKmCalculed() / (t.getCalculedDistance() / 1000.0);
        }
        return null;
    }

    /** Custo/hora usado no plano (custo de horas planejado ÷ horas planejadas). */
    private static Double plannedHourlyRate(Transport t) {
        if (t.getCostHourCalculed() == null || t.getTotalTimeCalculed() == null || t.getTotalTimeCalculed() <= 0) {
            return null;
        }
        return t.getCostHourCalculed() / (t.getTotalTimeCalculed() / 3600.0);
    }

    /** Custo fixo do perfil embutido no plano: total previsto − custo de km − custo de horas. */
    private static double plannedFixedCost(Transport t) {
        if (t.getTotalCostCalculed() == null) return 0.0;
        double fixed = t.getTotalCostCalculed()
                - (t.getCostKmCalculed() == null ? 0.0 : t.getCostKmCalculed())
                - (t.getCostHourCalculed() == null ? 0.0 : t.getCostHourCalculed());
        return Math.max(0.0, fixed);
    }

    private static Double round(Double v, int d) {
        if (v == null) return null;
        double f = Math.pow(10, d);
        return Math.round(v * f) / f;
    }
}
