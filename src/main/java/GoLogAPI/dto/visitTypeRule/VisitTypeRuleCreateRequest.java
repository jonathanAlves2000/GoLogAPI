package GoLogAPI.dto.visitTypeRule;

import GoLogAPI.model.enums.VisitTypeRuleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record VisitTypeRuleCreateRequest(
        @NotBlank(message = "O nome da regra é obrigatório")
        String name,

        String description,

        @NotNull(message = "O tipo da regra é obrigatório")
        VisitTypeRuleType ruleType,

        @NotNull(message = "O primeiro tipo de visita é obrigatório")
        UUID visitType1Id,

        @NotNull(message = "O segundo tipo de visita é obrigatório")
        UUID visitType2Id,

        UUID companyId
) { }
