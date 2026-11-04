package GoLogAPI.dto.shipmentType;

import jakarta.validation.constraints.Size;

public record DeliveryTypeUpdateRequest(
        @Size(max = 255, message = "O nome deve ter até 255 caracteres.")
        String name,

        @Size(max = 500, message = "A descrição deve ter até 500 caracteres.")
        String description,

        @Size(max = 250, message = "Cuidados deve ter até 250 caracteres.")
        String care
) implements DeliveryTypeRequest { }
