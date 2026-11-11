package GoLogAPI.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeFuel {
    DIESEL("DIESEL"),
    ETANOL("ETANOL"),
    GASOLINA("GASOLINA"),
    FLEX("FLEX"),
    GNV("GNV"),
    ELETRICO("ELETRICO");
    private final String fuel;
}
