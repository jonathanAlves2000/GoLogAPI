package GoLogAPI.dto.shipment;

import GoLogAPI.model.*;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ShipmentRequest {
    UUID id();
    TypeOperation typeOperation();
    Double weight();
    Double volume();
    LocalDateTime schedulind();
    String status();
    Integer ShippingSequence();
    User user();
    ShipmentType deliveryType();
    Transport transport();
    TypeTransport typeTransport();
    Address address();
    Company customer();
    Shipment OpeationOrigem();
}
