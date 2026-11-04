package GoLogAPI.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserProfile {
    ADMIN("Administrador"),
    DRIVER("Motorista"),
    OPERATOR("Operador"),
    POWERBI("POWER BI");

    private final String profile;
}
