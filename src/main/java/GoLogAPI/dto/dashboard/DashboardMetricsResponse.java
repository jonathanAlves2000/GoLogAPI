package GoLogAPI.dto.dashboard;

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
        double emissaoCo2Efetiva
) { }
