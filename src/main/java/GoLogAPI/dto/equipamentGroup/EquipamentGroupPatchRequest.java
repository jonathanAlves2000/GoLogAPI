package GoLogAPI.dto.equipamentGroup;

import java.util.UUID;

public record EquipamentGroupPatchRequest(
        String observation,
        UUID equipament1Id,
        UUID equipament2Id,
        UUID equipament3Id
) { }
