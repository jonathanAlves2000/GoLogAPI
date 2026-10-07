package GoLogAPI.dto.integration;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record InvoiceJsonBatchRequest(
        @NotEmpty(message = "A lista de faturas não pode estar vazia.")
        List<InvoiceImportItem> invoices
) { }
