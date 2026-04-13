package GoLogAPI.dto.equipamentGroup;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EquipamentGroupUpdateRequest(
        String observation,

        @NotNull
        UUID equipament1Id,

        UUID equipament2Id,
        UUID equipament3Id
) { }
