package GoLogAPI.dto.login;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Por Favor, insira o emial!")
        String email,

        @NotBlank(message = "Por favor, insira a senha!")
        String password
) { }
