package GoLogAPI.dto.shipmentDemand;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

public record ShipmentDemandRequest(
        @NotNull(message = "O ID do tipo de demanda é obrigatório")
        UUID demandTypeId,

        @NotNull(message = "A quantidade de demanda é obrigatória")
        @PositiveOrZero(message = "A quantidade de demanda deve ser maior ou igual a zero")
        Double amount
) { }
