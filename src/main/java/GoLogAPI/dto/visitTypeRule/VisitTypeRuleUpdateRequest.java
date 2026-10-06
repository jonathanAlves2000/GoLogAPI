package GoLogAPI.dto.visitTypeRule;

import GoLogAPI.model.enums.VisitTypeRuleType;

import java.util.UUID;

public record VisitTypeRuleUpdateRequest(
        String name,
        String description,
        VisitTypeRuleType ruleType,
        UUID visitType1Id,
        UUID visitType2Id
) { }
