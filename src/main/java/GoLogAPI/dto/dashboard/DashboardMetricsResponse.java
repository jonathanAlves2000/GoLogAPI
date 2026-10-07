package GoLogAPI.dto.dashboard;

import java.util.List;

public record DashboardMetricsResponse(
        long quantidadeMotoristas,
        long rotasEmAndamento,
        long quantidadeEntregasEmTransporte,
        long backlogEntregasPendentes,
        double taxaAlocacao,
        long rotasConcluidas,
        double slaDia,
        long atrasos,
        double custoTotalPlanejado,
        double custoTotalEfetivo,
        double emissaoCo2Planejada,
        double emissaoCo2Efetiva,
        double tKmRealizado,
        double tKmPlanejado,
        double tKmEconomizado,
        double co2EconomizadoKg,
        double dieselEconomizadoLitros,
        double percentualReducaoCo2,
        List<SustainabilityHistoryPoint> historicoSustentabilidade
) {
    public DashboardMetricsResponse(
            long quantidadeMotoristas,
            long rotasEmAndamento,
            long quantidadeEntregasEmTransporte,
            long backlogEntregasPendentes,
            double taxaAlocacao,
            long rotasConcluidas,
            double slaDia,
            long atrasos,
            double custoTotalPlanejado,
            double custoTotalEfetivo,
            double emissaoCo2Planejada,
            double emissaoCo2Efetiva
    ) {
        this(
                quantidadeMotoristas,
                rotasEmAndamento,
                quantidadeEntregasEmTransporte,
                backlogEntregasPendentes,
                taxaAlocacao,
                rotasConcluidas,
                slaDia,
                atrasos,
                custoTotalPlanejado,
                custoTotalEfetivo,
                emissaoCo2Planejada,
                emissaoCo2Efetiva,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                0.0,
                List.of()
        );
    }
}
