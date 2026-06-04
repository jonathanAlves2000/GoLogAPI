package GoLogAPI.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeFuel {
    DIESEL("DIESEL"),
    ETANOL("ETANOL"),
    GASOLINA("GASOLINA");
    private final String fuel;
}
