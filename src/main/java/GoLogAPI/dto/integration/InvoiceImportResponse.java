package GoLogAPI.dto.integration;

import java.util.List;
import java.util.UUID;

public record InvoiceImportResponse(
        int totalReceived,
        int successCount,
        int errorCount,
        List<UUID> createdShipmentIds,
        List<String> errors,
        long processingTimeMs
) { }
