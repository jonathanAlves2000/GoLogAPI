package GoLogAPI.dto.supportedType;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SupportedTypeCreateRequest(
        @NotNull(message = "Id do conjunto de transportes não pode ser nulo.")
        UUID groupId,

        @NotNull(message = "Id do tipo de transporte não pode ser nulo.")
        UUID typeId
) { }
