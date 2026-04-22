package GoLogAPI.dto.deliveryType;

import jakarta.validation.constraints.Size;

public interface DeliveryTypeRequest {
    String name();
    String description();
    String care();
}
