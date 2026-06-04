package GoLogAPI.dto.trailer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record TrailerCreateRequest(

        @NotBlank(message = "Placa do equipamento deve ser informada.")
        @Pattern(regexp = "^[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}$", message = "Placa inválida")
        String plate,

        @NotBlank(message = "RENAVAM do equipamento deve ser informado.")
        @Pattern(regexp = "^[0-9]{11}$", message = "RENAVAM deve conter exatamente 11 dígitos numéricos")
        String renavam,

        @NotBlank(message = "Modelo deve ser informado.")
        String model,

        @NotNull(message = "Numero de eixos deve ser informado.")
        Integer numberAxles,

        @NotNull(message = "Capacidade maxima deve ser informada.")
        Double maximumCapacity,

        @NotNull(message = "Volume maximo deve ser informado.")
        Double maximumVolume,

        @NotNull(message = "A Empresa do equipamento deve ser informada.")
        UUID companyId

) implements TrailerRequest{ }
