package GoLogAPI.service.sustainability;

import GoLogAPI.model.Tractor;
import GoLogAPI.model.enums.TypeFuel;

/**
 * Fatores de emissão de CO₂ por litro de combustível (combustão direta, escopo 1).
 *
 * <p>Fonte: IPCC 2006 Guidelines for National Greenhouse Gas Inventories, Vol. 2, cap. 3 (Mobile Combustion),
 * convertidos para kg CO₂ por litro com a densidade de referência — valores amplamente adotados também pelo
 * GHG Protocol: diesel ≈ 2,68 kg CO₂/L e gasolina ≈ 2,31 kg CO₂/L.</p>
 *
 * <p>Premissas declaradas (para defesa metodológica):</p>
 * <ul>
 *   <li>Gasolina: o fator é o da gasolina pura; aplicado à gasolina C brasileira (com etanol anidro) é conservador,
 *       isto é, superestima a parcela fóssil.</li>
 *   <li>Etanol: emissão de combustão é biogênica e, pela convenção do IPCC, não é somada ao CO₂ fóssil (fator 0).</li>
 *   <li>Elétrico: zero emissão direta; a emissão da geração de energia (escopo 2) não é contabilizada.</li>
 *   <li>Flex e GNV: a mistura/unidade real é desconhecida, então nenhum fator é presumido — o cadastro precisa
 *       informar {@code co2PerKilometer} explicitamente para entrarem na conta.</li>
 * </ul>
 */
public final class EmissionFactors {

    public static final double DIESEL_KG_CO2_PER_LITER = 2.68;
    public static final double GASOLINE_KG_CO2_PER_LITER = 2.31;

    private EmissionFactors() {
    }

    /** kg CO₂ por litro do combustível, ou {@code null} quando não há fator seguro. */
    public static Double kgCo2PerLiter(TypeFuel fuel) {
        if (fuel == null) return null;
        return switch (fuel) {
            case DIESEL -> DIESEL_KG_CO2_PER_LITER;
            case GASOLINA -> GASOLINE_KG_CO2_PER_LITER;
            case ETANOL, ELETRICO -> 0.0;
            case FLEX, GNV -> null;
        };
    }

    /**
     * kg CO₂/km de um veículo: valor informado no cadastro; senão fator do combustível ÷ consumo (km/L);
     * senão {@code null} (sem dado suficiente — o veículo fica fora dos totais e da cobertura).
     */
    public static Double kgCo2PerKm(Tractor tractor) {
        if (tractor == null) return null;
        if (tractor.getCo2PerKilometer() != null) return tractor.getCo2PerKilometer();
        Double factor = kgCo2PerLiter(tractor.getTypeFuel());
        if (factor == null) return null;
        if (factor == 0.0) return 0.0;
        Double kmPerLiter = tractor.getKmPerLiter();
        if (kmPerLiter == null || kmPerLiter <= 0) return null;
        return factor / kmPerLiter;
    }
}
