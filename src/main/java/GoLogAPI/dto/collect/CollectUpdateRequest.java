package GoLogAPI.dto.collect;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record CollectUpdateRequest(
        @Positive(message = "A sequencia deve ser um número positivo.")
        @Min(value = 1, message = "A sequência deve ser no mínimo 1.")
        @Max(value = 99999, message = "A sequência deve ser no máximo 9999.")
        Integer sequence,
        UUID collectionAddressId,
        UUID customerCollectsId
) { }
