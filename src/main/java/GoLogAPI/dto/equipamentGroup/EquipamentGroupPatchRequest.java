package GoLogAPI.dto.equipamentGroup;

import java.util.UUID;

public record EquipamentGroupPatchRequest(
        String observation,
        UUID equipamentId1,
        UUID equipamentId2,
        UUID equipamentId3
) { }
