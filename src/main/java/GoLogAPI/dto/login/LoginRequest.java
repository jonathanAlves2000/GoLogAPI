package GoLogAPI.dto.login;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Por Favor, insira o email!")
        String email,

        @NotBlank(message = "Por favor, insira a senha!")
        String password
) { }
