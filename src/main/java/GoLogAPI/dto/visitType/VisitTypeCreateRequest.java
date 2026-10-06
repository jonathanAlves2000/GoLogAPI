package GoLogAPI.dto.visitType;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record VisitTypeCreateRequest(
        @NotBlank(message = "O código é obrigatório")
        String code,

        @NotBlank(message = "O nome é obrigatório")
        String name,

        String description,

        UUID companyId
) { }
