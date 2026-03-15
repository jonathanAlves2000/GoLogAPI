package GoLogAPI.dto.equipamentGroup;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EquipamentGroupCreateRequest(
        String observation,
        @NotNull
        UUID equipamentId1,
        UUID equipamentId2,
        UUID equipamentId3
){}
