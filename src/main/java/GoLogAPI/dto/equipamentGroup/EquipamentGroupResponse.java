package GoLogAPI.dto.equipamentGroup;

import GoLogAPI.dto.equipament.EquipamentResponse;

import java.util.UUID;

public record EquipamentGroupResponse(
       UUID id,
       String observation,
       EquipamentResponse equipament1,
       EquipamentResponse equipament2,
       EquipamentResponse equipament3
) { }
