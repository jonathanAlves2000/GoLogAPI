package GoLogAPI.dto.deliveryType;

import java.util.UUID;

public record DeliveryTypeResponse(
        UUID id,
        String name,
        String description,
        String care
) { }
