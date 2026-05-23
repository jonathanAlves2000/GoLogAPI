package GoLogAPI.dto.delivery;

import GoLogAPI.model.*;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryResponseList(
        UUID id,
        Double weight,
        Double volume,
        LocalDateTime scheduledCollection,
        LocalDateTime scheduledDelivery,
        String routePlanned,
        String routeCompleted,
        String status,
        Integer deliverySequence,
        UUID user,
        UUID deliveryType,
        UUID transport,
        UUID typeTransport,
        UUID originAdrress,
        UUID destinationAddress,
        UUID customerCollects,
        UUID customerDelivery
) { }
