package GoLogAPI.dto.dashboard;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Indicadores do painel. Todos são calculados a partir de dados registrados no sistema — nenhum percentual
 * ou fator é presumido. Campos podem vir {@code null} quando não há base para o cálculo (ex.: SLA sem
 * nenhuma entrega concluída); o front deve exibir "sem dados" nesses casos.
 *
 * <p>{@code methodology} traz, por indicador, a fórmula e a fonte usadas (exibidas como dica na tela).</p>
 */
public record DashboardMetricsResponse(
        LocalDateTime generatedAt,
        Operation operation,
        Sla sla,
        Planning planning,
        Emissions emissions,
        Sequencing sequencing,
        List<DailyPoint> daily,
        Map<String, String> methodology
) {
    /** Situação atual da operação. */
    public record Operation(
            long activeDrivers,
            long activeVehicles,
            long shipmentsPending,
            long shipmentsPlanned,
            long shipmentsInProgress,
            long shipmentsDelivered,
            long shipmentsOverdue,
            long todayShipments,
            long todayDelivered,
            long tripsPlanned,
            long tripsInProgress,
            long tripsFinished
    ) { }

    /** Pontualidade das entregas concluídas com horário de conclusão registrado. */
    public record Sla(
            long deliveredWithTimestamp,
            long deliveredOnTime,
            Double onTimeRate,
            long deliveredWithoutTimestamp
    ) { }

    /** Totais do plano gerado pelo otimizador (Google Route Optimization). */
    public record Planning(
            long trips,
            long plannedStops,
            double plannedKm,
            double plannedHours,
            double plannedCost,
            Double avgStopsPerTrip,
            Double avgLoadFactor,
            long tripsWithCapacity,
            double tonKm
    ) { }

    /** Emissões de CO₂ (escopo 1) — só viagens cujo veículo tem fator de emissão determinável. */
    public record Emissions(
            Double plannedCo2Kg,
            Double realizedCo2Kg,
            long tripsWithFactor,
            long tripsTotal,
            Double co2PerDeliveryKg,
            Double gCo2PerTonKm,
            double realizedKm,
            long tripsWithRealizedKm
    ) { }

    /** Ganho do sequenciamento otimizado vs. ordem de agendamento, medido em linha reta (haversine). */
    public record Sequencing(
            long tripsEvaluated,
            double optimizedStraightKm,
            double baselineStraightKm,
            double savedStraightKm,
            Double savedPercent
    ) { }

    /** Série diária baseada na data agendada das remessas roteirizadas. */
    public record DailyPoint(
            LocalDate date,
            long shipments,
            long delivered,
            double plannedKm,
            Double plannedCo2Kg
    ) { }
}
