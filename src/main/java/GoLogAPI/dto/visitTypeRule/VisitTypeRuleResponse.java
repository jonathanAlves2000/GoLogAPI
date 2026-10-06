package GoLogAPI.dto.visitTypeRule;

import GoLogAPI.dto.visitType.VisitTypeResponse;
import GoLogAPI.model.enums.VisitTypeRuleType;

import java.util.UUID;

public record VisitTypeRuleResponse(
        UUID id,
        String name,
        String description,
        VisitTypeRuleType ruleType,
        VisitTypeResponse visitType1,
        VisitTypeResponse visitType2,
        UUID companyId
) { }
