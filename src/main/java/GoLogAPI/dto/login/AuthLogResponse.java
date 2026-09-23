package GoLogAPI.dto.login;

import jakarta.persistence.Column;

import java.time.Instant;
import java.util.UUID;

public record AuthLogResponse(
        UUID id,
        Instant createdAt,
        String createdBy,
        Instant updatedAt,
        String updatedBy
) { }
