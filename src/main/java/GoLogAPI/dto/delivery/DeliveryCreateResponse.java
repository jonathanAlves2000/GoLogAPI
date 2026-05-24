package GoLogAPI.dto.delivery;

import GoLogAPI.model.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryCreateResponse(
        UUID id,
        Double weight,
        Double volume,
        LocalDateTime scheduledCollection,
        LocalDateTime scheduledDelivery,
        String routePlanned,
        String routeCompleted,
        String status,
        Integer deliverySequence,
        UUID userId,
        UUID deliveryTypeId,
        UUID transportId,
        UUID typeTransportId,
        UUID deliveryAddressId,
        UUID customerDeliveryId,
        UUID collectId
) { }
