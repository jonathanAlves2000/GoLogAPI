package GoLogAPI.dto.trailer;

import jakarta.validation.constraints.Pattern;

public record TrailerUpdateRequest(

        @Pattern(regexp = "^[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}$", message = "Placa inválida")
        String plate,

        @Pattern(regexp = "^[0-9]{11}$", message = "O RENAVAM deve conter exatamente 11 dígitos numéricos")
        String renavam,

        String model,

        Integer numberAxles,

        Double maximumCapacity,

        Double maximumVolume
) implements TrailerRequest{ }
