package GoLogAPI.dto.transport;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record TransportCreateRequest(
        
        @Size(min = 5, max = 500, message = "A rota realizada deve ter entre 5 e 500 caracteres")
        String routeCompleted,

        @Positive(message = "A quantidade de entregas deve ser maior que zero.")
        Integer deliveryQuantity,

        @PositiveOrZero(message = "A quilometragem não pode ser negativa.")
        Integer distanceTraveled,

        @PositiveOrZero(message = "O tempo parado não pode ser negativo.")
        Double timeStopped,

        @Positive(message = "O tempo total deve ser maior que zero")
        Double totalTime,

        @NotNull(message = "O Id do motorista deve ser informado.")
        UUID driverId,

        @NotNull(message = "O Id do transportador deve ser informado.")
        UUID transporterId,

        @NotNull(message = "O Id do conjunto de equipamentos deve ser informado.")
        UUID equipamentGroupId
) { }
