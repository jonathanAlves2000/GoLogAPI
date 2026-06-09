package GoLogAPI.dto.login;

import java.util.UUID;

public record TokenResponse(
        String token,
        UUID userId
) { }
