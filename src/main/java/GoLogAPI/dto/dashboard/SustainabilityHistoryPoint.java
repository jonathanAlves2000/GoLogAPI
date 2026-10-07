package GoLogAPI.dto.dashboard;

public record SustainabilityHistoryPoint(
        String periodo,
        double tKmEconomizado,
        double co2EconomizadoKg,
        double distanciaEconomizadaKm
) {}
