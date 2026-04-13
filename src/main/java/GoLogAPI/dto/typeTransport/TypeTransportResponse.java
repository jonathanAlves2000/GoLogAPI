package GoLogAPI.dto.typeTransport;

import java.util.UUID;

public record TypeTransportResponse(
        UUID id,
        String name,
        String description,
        String care
) { }
