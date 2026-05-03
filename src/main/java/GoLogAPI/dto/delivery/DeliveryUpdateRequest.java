package GoLogAPI.dto.delivery;

import GoLogAPI.model.*;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliveryUpdateRequest(
        @Positive
        Double weight,
        @Positive
        Double volume,
        @FutureOrPresent
        LocalDateTime scheduledCollection,
        @FutureOrPresent
        LocalDateTime scheduledDelivery,
        @Size(min = 5, max = 500)
        String routePlanned,
        @Size(min = 5, max = 500)
        String routeCompleted,
        String status,
        Integer deliverySequence,
        UUID userId,
        UUID deliveryTypeId,
        UUID transportId,
        UUID typeTransportId,
        UUID originAdrressId,
        UUID destinationAddressId,
        UUID customerCollectsId,
        UUID customerDeliveryId
) {
}
