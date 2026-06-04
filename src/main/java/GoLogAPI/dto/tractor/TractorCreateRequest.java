package GoLogAPI.dto.tractor;

import GoLogAPI.model.TypeFuel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record TractorCreateRequest(

        @NotBlank(message = "Placa do equipamento deve ser informada.")
        @Pattern(regexp = "^[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}$", message = "Placa inválida")
        String plate,

        @NotBlank(message = "RENAVAM do equipamento deve ser informado.")
        @Pattern(regexp = "^[0-9]{11}$", message = "O RENAVAM deve conter exatamente 11 dígitos numéricos")
        String renavam,

        @NotBlank(message = "Modelo do equipamento deve ser informado.")
        String model,

        @NotNull(message = "Quantidade de eixos do equipamento deve ser informada.")
        Integer numberAxles,

        @NotNull(message = "Capacidade maxima do equipamento deve ser informada.")
        Double maximumCapacity,

        @Schema(name = "Type Fuel")
        @NotNull
        TypeFuel typeFuel,

        @NotNull
        Double kmPerLiter,

        @NotNull(message = "Empresa do equipamento deve ser informada.")
        UUID companyId

) implements TractorRequest{ }
