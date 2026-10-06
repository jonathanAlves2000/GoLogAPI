package GoLogAPI.dto.equipamentCapacity;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record EquipamentCapacityRequest(
        @NotNull(message = "O ID do tipo de demanda é obrigatório")
        UUID demandTypeId,

        @NotNull(message = "A capacidade máxima é obrigatória")
        @Positive(message = "A capacidade máxima deve ser maior que zero")
        Double maxCapacity,

        Double softMaxCapacity,
        Double costPerUnitAboveSoftMax
) { }
