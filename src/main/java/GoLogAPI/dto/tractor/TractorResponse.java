package GoLogAPI.dto.tractor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record TractorResponse(
        UUID id,
        String plate,
        String renavam,
        String model,
        Integer namberAxles,
        Double maximumCapacity,
        String typeFuel
) { }
