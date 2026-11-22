package GoLogAPI.dto.company;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record CompanyUpdateRequest(

        @Pattern(regexp = "^$|^[A-Za-z0-9À-ÿ\\s.,/\\-&'’()#@+]+$", message = "Nome inválido")
        @Size(max = 150, message = "Nome deve ter até 150 caracteres")
        String legalName,

        Boolean isCliente,

        @Pattern(regexp = "^$|^\\+?[0-9()\\s\\-.]{8,20}$", message = "Telefone inválido")
        String phoneNumber,

        @Email(message = "Email inválido")
        String email,

        @Pattern(regexp = "^$|^(\\d{11}|\\d{14}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2})$", message = "CPF ou CNPJ inválido")
        String cnpjCpf,

        UUID addressId,

        /** Raio do geofence (m): vazio mantém o atual, 0 volta ao padrão da transportadora, 100–5000 define. */
        @jakarta.validation.constraints.Min(value = 0, message = "O raio do geofence não pode ser negativo")
        @jakarta.validation.constraints.Max(value = 5000, message = "O raio do geofence deve ser de no máximo 5000 m")
        Integer geofenceRadiusMeters
) implements CompanyRequest{ }
