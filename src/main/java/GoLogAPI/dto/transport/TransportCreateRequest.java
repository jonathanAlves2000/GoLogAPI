package GoLogAPI.dto.transport;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record TransportCreateRequest(

        @NotBlank(message = "A rota planejada é obrigatória.")
        @Size(min = 5, max = 500, message = "A rota planejada deve ter entre 5 e 500 caracteres")
        String routeReturnPlanned,

        @NotBlank(message = "A rota realizada é obrigatória.")
        @Size(min = 5, max = 500, message = "A rota realizada deve ter entre 5 e 500 caracteres")
        String routeReturnCompleted,

        @NotNull(message = "A quantidade de entregas não poder ser nula.")
        @Positive(message = "A quantidade de entregas deve ser maior que zero.")
        Integer deliveryQuantity,

        @NotNull(message = "A quilometragem total não poder ser nula.")
        @PositiveOrZero(message = "A quilometragem não pode ser negativa.")
        Integer totalKilometer,

        @NotNull(message = "O tempo parado não pode ser nulo.")
        @PositiveOrZero(message = "O tempo parado não pode ser negativo.")
        Double timeStopped,

        @NotNull(message = "O tempo toal não pode ser nulo.")
        @Positive(message = "O tempo total deve ser maior que zero")
        Double totalTime,

        @NotNull(message = "O Id do motorista não pode ser nulo.")
        UUID driverId,

        @NotNull(message = "O Id do transportador não pode ser nulo.")
        UUID transporterId,

        @NotNull(message = "O Id do conjunto de equipamentos não pode ser nulo.")
        UUID equipamentGroupId
) { }
