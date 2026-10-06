package GoLogAPI.dto.shipment;

import java.time.LocalDateTime;
import java.util.UUID;

import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.model.enums.TypeOperation;

public interface ShipmentRequest {
    TypeOperation typeOperation();
    Double weight();
    Double volume();
    LocalDateTime schedulind();
    ShipmentStatus status();
    UUID userId();
    UUID shipmentTypeId();
    UUID typeTransportId();
    UUID addressId();
    UUID customerId();
    UUID operationOrigemId();
}
