package GoLogAPI.dto.delivery;

import GoLogAPI.model.*;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DeliveryRequest {
    UUID id();
    Double weight();
    Double volume();
    LocalDateTime scheduledCollection();
    LocalDateTime scheduledDelivery();
    String routePlanned();
    String routeCompleted();
    String status();
    Integer deliverySequence();
    User user();
    DeliveryType deliveryType();
    Transport transport();
    TypeTransport typeTransport();
    Address deliveryAddress();
    Company customerDelivery();
    Collect collect();
}
