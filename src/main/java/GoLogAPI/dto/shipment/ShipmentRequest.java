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
    User user();
    ShipmentType deliveryType();
    TypeTransport typeTransport();
    Address address();
    Company customer();
    Shipment OpeationOrigem();
}
