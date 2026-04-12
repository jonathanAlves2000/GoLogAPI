package GoLogAPI.dto.typeTransport;

import jakarta.validation.constraints.Pattern;

public record TypeTransportUpdateRequest(
        @Pattern(
                regexp = "^[A-Z ]+$",
                message = "O nome deve conter apenas letras maiúsculas de A-Z (sem acentos) e espaços"
        )
        String name,
        String description,
        String care
) { }
