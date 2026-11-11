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
 */
@Getter
@AllArgsConstructor
public enum VehicleType {
    MOTO("Moto", true),
    CARRO("Carro de passeio", true),
    UTILITARIO("Utilitário leve (Fiorino/Saveiro)", true),
    VAN("Van / furgão", true),
    VUC("VUC (Veículo Urbano de Carga)", true),
    TOCO("Caminhão toco (3/4)", true),
    TRUCK("Caminhão truck", true),
    BITRUCK("Caminhão bitruck", true),
    CAVALO_MECANICO("Cavalo mecânico (exige carreta)", false);

    private final String description;
    private final boolean rigid;
}
