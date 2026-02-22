package GoLogAPI.dto.equipament;

import java.util.UUID;

public record EuipamentResponse(

        UUID id,
        String plate,
        String renavam,
        String model,
        Integer namberAxles,
        Double maximumCapacity
) { }
