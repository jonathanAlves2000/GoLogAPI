package GoLogAPI.dto.demandType;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record DemandTypeCreateRequest(
        @NotBlank(message = "O código é obrigatório")
        String code,

        @NotBlank(message = "O nome é obrigatório")
        String name,

        @NotBlank(message = "A unidade de medida é obrigatória")
        String unit,

        String description,

        UUID companyId
) { }
