package GoLogAPI.dto.tractor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TractorPatchRequest(
        @NotBlank(message = "Campo placa vazio")
        @Pattern(regexp = "^[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}$", message = "Placa inválida")
        String plate,

        @NotBlank(message = "Campo renavan vazio")
        @Pattern(regexp = "^[0-9]{11}$", message = "O RENAVAM deve conter exatamente 11 dígitos numéricos")
        String renavam,

        @NotBlank(message = "Campo modelo vazio")
        String model,

        @NotNull(message = "Campo numero de eixos vazio")
        Integer namberAxles,

        @NotNull(message = "Campo capcidade maxima vazio")
        Double maximumCapacity,

        String typeFuel
) { }
