package GoLogAPI.exception;

import java.time.Instant;
import java.util.List;

/**
 * Corpo único de erro da API. O front-end lê {@code message} (sempre presente) e, quando houver,
 * a lista detalhada em {@code errors} (ex.: um item por campo inválido).
 */
public record ApiError(
        int status,
        String message,
        List<String> errors,
        Instant timestamp
) {
    public static ApiError of(int status, String message, List<String> errors) {
        return new ApiError(status, message, errors == null ? List.of() : errors, Instant.now());
    }

    public static ApiError of(int status, String message) {
        return of(status, message, List.of());
    }
}
