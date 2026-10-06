package GoLogAPI.dto.login;

import java.util.UUID;

public record TokenResponse(
        String token,
        UUID userId,
        String userName,
        String userRole,
        UUID companyId,
        String companyName,
        String companyType,
        Boolean isMaster
) {
    public TokenResponse(String token, UUID userId) {
        this(token, userId, null, null, null, null, null, false);
    }
}
