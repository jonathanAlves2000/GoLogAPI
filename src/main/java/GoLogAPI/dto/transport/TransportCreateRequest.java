package GoLogAPI.dto.transport;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record TransportCreateRequest(

        @NotBlank(message = "A rota planejada deve ser informada.")
        @Size(min = 5, max = 500, message = "A rota planejada deve ter entre 5 e 500 caracteres")
        String routeReturnPlanned,

        @NotBlank(message = "A rota realizada deve ser informada.")
        @Size(min = 5, max = 500, message = "A rota realizada deve ter entre 5 e 500 caracteres")
        String routeReturnCompleted,

        @NotNull(message = "A quantidade de entregas deve ser informada.")
        @Positive(message = "A quantidade de entregas deve ser maior que zero.")
        Integer deliveryQuantity,

        @NotNull(message = "A quilometragem total deve ser informada.")
        @PositiveOrZero(message = "A quilometragem não pode ser negativa.")
        Integer totalKilometer,

        @NotNull(message = "O tempo parado deve ser informado.")
        @PositiveOrZero(message = "O tempo parado não pode ser negativo.")
        Double timeStopped,

        @NotNull(message = "O tempo toal deve ser informado.")
        @Positive(message = "O tempo total deve ser maior que zero")
        Double totalTime,

        @NotNull(message = "O Id do motorista deve ser informado.")
        UUID driverId,

        @NotNull(message = "O Id do transportador deve ser informado.")
        UUID transporterId,

        @NotNull(message = "O Id do conjunto de equipamentos deve ser informado.")
        UUID equipamentGroupId
) { }
