package GoLogAPI.dto.equipamentGroup;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record equipamentGroupPatchRequest(
        String observation,
        UUID equipament1,
        UUID equipament2,
        UUID equipament3
) { }
