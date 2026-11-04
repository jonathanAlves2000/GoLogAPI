package GoLogAPI.dto.shipmentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeliveryTypeCreateRequest(
        @NotBlank(message = "O nome não pode ser vazio.")
        @Size(min = 2, max = 255, message = "O nome deve ter entre 2 e 255 caracteres.")
        String name,

        @NotBlank(message = "A descrição não pode ser vazia")
        @Size(min = 2, max = 500, message = "A descrição deve ter entre 2 e 500 caracteres.")
        String description,

        @Size(max = 250, message = "Cuidados deve ter até 250 caracteres.")
        String care
) implements DeliveryTypeRequest { }
