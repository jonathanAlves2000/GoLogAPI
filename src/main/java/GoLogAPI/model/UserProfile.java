package GoLogAPI.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserProfile {
    ADMIN("ADMIN"),
    DRIVER("DRIVER"),
    OPERATOR("OPERATOR");

    private final String profile;
}
