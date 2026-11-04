package GoLogAPI.dto.typeTransport;

import jakarta.validation.constraints.Pattern;

public record TypeTransportUpdateRequest(
        @Pattern(
                regexp = "^$|^[A-Za-z0-9À-ÿ _-]+$",
                message = "O nome deve conter apenas letras, números, espaços, hífen ou underline"
        )
        String name,
        String description,
        String care
) { }
