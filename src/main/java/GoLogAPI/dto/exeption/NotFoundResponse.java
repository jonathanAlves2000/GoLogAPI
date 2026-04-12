package GoLogAPI.dto.exeption;

import java.util.UUID;

public record NotFoundResponse(
        String message,
        UUID id
) {
}
