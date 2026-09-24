package GoLogAPI.dto.demandType;

import java.util.UUID;

public record DemandTypeResponse(
        UUID id,
        String code,
        String name,
        String unit,
        String description,
        Boolean isSystemDefault,
        UUID companyId
) { }
