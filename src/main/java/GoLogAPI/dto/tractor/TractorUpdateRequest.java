package GoLogAPI.dto.tractor;

import GoLogAPI.model.Company;
import GoLogAPI.model.TypeFuel;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record TractorUpdateRequest(

        @Pattern(regexp = "^[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}$", message = "Placa inválida")
        String plate,

        @Pattern(regexp = "^[0-9]{11}$", message = "O RENAVAM deve conter exatamente 11 dígitos numéricos")
        String renavam,

        String model,
        Integer numberAxles,
        Double maximumCapacity,
        TypeFuel typeFuel,
        Double kmPerLiter,
        UUID companyId

) implements TractorRequest { }
