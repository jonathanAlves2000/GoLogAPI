package GoLogAPI.dto.collect;

import GoLogAPI.dto.company.CompanyCreateRequest;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record CollectCreateRequest(
        @NotNull(message = "A sequencia não pode ser nula.")
        @Positive(message = "A sequencia deve ser um número positivo.")
        @Min(value = 1, message = "A sequência deve ser no mínimo 1.")
        @Max(value = 99999, message = "A sequência deve ser no máximo 9999.")
        Integer sequence,
        @NotNull(message = "O endereço de coleta não pode ser nulo.")
        UUID collectionAddressId,
        @NotNull(message = "O cliente de coleta não pode ser nulo.")
        UUID customerCollectsId
) { }
