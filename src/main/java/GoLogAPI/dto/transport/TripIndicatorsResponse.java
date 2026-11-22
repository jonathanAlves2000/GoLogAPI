package GoLogAPI.dto.transport;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Indicadores realizados de uma viagem, calculados sobre a rota recebida por telemetria.
 * Sem rota de telemetria, distância, t·km, combustível, CO₂ e custo vêm zerados. Com rota, um valor
 * {@code null} significa que falta cadastro para calculá-lo (ex.: veículo sem consumo km/L) — nunca presumido.
 */
public record TripIndicatorsResponse(
        UUID transportId,
        boolean hasTelemetryRoute,
        LocalDateTime trackingStartedAt,
        LocalDateTime lastTelemetryAt,
        double distanceTraveledKm,
        double elapsedHours,
        double tonKm,
        Double fuelLiters,
        Double co2Kg,
        Double effectiveCost,
        Double effectiveCostPerKm,
        CostBreakdown cost,
        ScheduleAdherence schedule,
        VehicleFactors factors,
        /** Polilinha (Encoded Polyline) montada com os pontos de telemetria que têm o transport_id da viagem. */
        String routeCompleted
) {
    /** Custo efetivo = km realizados × custo/km do veículo + horas realizadas × custo/hora do plano + custo fixo do plano. */
    public record CostBreakdown(double kmCost, double hourCost, double fixedCost) {
    }

    /** Paradas com horário agendado: atendidas até o horário, atendidas com atraso / vencidas, e ainda no prazo. */
    public record ScheduleAdherence(int scheduled, int metOnTime, int late, int pending, Double onTimeRate,
                                    int byGeofence, int byDeliveryRecord) {
    }

    public record VehicleFactors(Double kmPerLiter, Double kgCo2PerKm, Double costPerKm, Double costPerHour) {
    }
}
