package GoLogAPI.dto.transport;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record TransportUpdateRequest(
        @Size(min = 5, max = 500,message = "A rota planejada deve ter entre 5 e 500 caracteres")
        String routeReturnPlanned,

        @Size(min = 5, max = 255, message = "A rota realizada deve ter entre 5 e 255 caracteres")
        String routeReturnCompleted,

        @PositiveOrZero(message = "A quantidade de entregas deve ser maior que zero.")
        Integer deliveryQuantity,

        @PositiveOrZero(message = "A quilometragem não pode ser negativa.")
        Integer totalKilometer,

        @PositiveOrZero(message = "O tempo parado não pode ser negativo.")
        Double timeStopped,

        @PositiveOrZero(message = "O tempo total deve ser maior que zero")
        Double totalTime,

        UUID driverId,
        UUID transporterId,
        UUID equipamentGroupId
) { }
