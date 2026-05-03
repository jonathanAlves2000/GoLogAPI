package GoLogAPI.dto.deliveryType;

import jakarta.validation.constraints.Size;

public record DeliveryTypeUpdateRequest(
        @Size(min = 5, max = 255, message = "O nome deve ter entre 5 e 50 caracteres.")
        String name,

        @Size(min = 5, max = 500, message = "A descrição deve ter entre 5 e 500 caracteres.")
        String description,

        @Size(min = 5, max = 250, message = "Cuidados deve ter entre 5 e 250 caracteres.")
        String care
) implements DeliveryTypeRequest { }
