package GoLogAPI.dto.shipment;

import GoLogAPI.model.*;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.model.enums.TypeOperation;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.UUID;

public record ShipmentResponseList(
        UUID id,
        TypeOperation typeOperation,
        Double weight,
        Double volume,
        LocalDateTime shedulind,
        ShipmentStatus status,
        @JsonIgnoreProperties("company")
        User user,
        ShipmentType shipmentType,
        TypeTransport typeTransport,
        Address address,
        Company customer,
        Shipment operationOrigem
) { }
