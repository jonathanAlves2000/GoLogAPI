package GoLogAPI.dto.shipmentType;

import java.util.UUID;

public record DeliveryTypeResponse(
        UUID id,
        String name,
        String description,
        String care
) { }
