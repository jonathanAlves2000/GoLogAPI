package GoLogAPI.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Categoria do veículo de tração.
 *
 * <p>Veículos <b>rígidos</b> (moto, carro, utilitário, van, VUC, toco, truck, bitruck) carregam a carga no
 * próprio chassi e operam sozinhos: ao serem cadastrados ganham automaticamente um conjunto de veículo
 * único, pronto para escala e roteirização. Apenas o {@link #CAVALO_MECANICO} precisa de carretas
 * acopladas via cadastro de conjuntos.</p>
 *
 * <p>{@code co2KgPerKm} é a emissão média de referência usada quando o cadastro não informa uma própria.</p>
 */
@Getter
@AllArgsConstructor
public enum VehicleType {
    MOTO("Moto", true, 0.08),
    CARRO("Carro de passeio", true, 0.14),
    UTILITARIO("Utilitário leve (Fiorino/Saveiro)", true, 0.17),
    VAN("Van / furgão", true, 0.25),
    VUC("VUC (Veículo Urbano de Carga)", true, 0.45),
    TOCO("Caminhão toco (3/4)", true, 0.60),
    TRUCK("Caminhão truck", true, 0.75),
    BITRUCK("Caminhão bitruck", true, 0.85),
    CAVALO_MECANICO("Cavalo mecânico (exige carreta)", false, 1.05);

    private final String description;
    private final boolean rigid;
    private final double co2KgPerKm;
}
