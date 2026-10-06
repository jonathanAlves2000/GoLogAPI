package GoLogAPI.dto.tractor;

import java.util.UUID;

import jakarta.validation.constraints.Pattern;

public record TractorUpdateRequest(

        @Pattern(regexp = "^[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}$", message = "Placa inválida")
        String plate,

        @Pattern(regexp = "^[0-9]{11}$", message = "O RENAVAM deve conter exatamente 11 dígitos numéricos")
        String renavam,

        String model,
        Integer numberAxles,
        Double maximumCapacity,
        Double costPerKilometer,
        UUID companyId

) implements TractorRequest { }
