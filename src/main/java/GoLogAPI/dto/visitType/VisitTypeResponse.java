package GoLogAPI.dto.visitType;

import java.util.UUID;

public record VisitTypeResponse(
        UUID id,
        String code,
        String name,
        String description,
        UUID companyId
) { }
