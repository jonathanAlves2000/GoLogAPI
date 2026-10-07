package GoLogAPI.dto.telemetry;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record TelemetryBatchRequest(
        @NotEmpty(message = "A lista de itens de telemetria não pode estar vazia.")
        List<TelemetryBatchItemRequest> items
) { }
