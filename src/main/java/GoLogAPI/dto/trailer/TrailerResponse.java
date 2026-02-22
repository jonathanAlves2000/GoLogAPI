package GoLogAPI.dto.trailer;

import java.util.UUID;

public record TrailerResponse(
        UUID id,
        String plate,
        String renavam,
        String model,
        Integer namberAxles,
        Double maximumCapacity,
        Double maximumVolume
) { }
