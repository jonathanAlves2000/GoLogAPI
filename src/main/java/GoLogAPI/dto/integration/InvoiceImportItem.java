package GoLogAPI.dto.integration;

import GoLogAPI.model.enums.TypeOperation;

import java.time.LocalDateTime;

public record InvoiceImportItem(
        String invoiceNumber,
        String series,
        String accessKey,
        TypeOperation typeOperation,
        Double weight,
        Double volume,
        Double value,
        LocalDateTime scheduledDate,
        String notes,
        String senderName,
        String senderDocument,
        String customerName,
        String customerDocument,
        String customerEmail,
        String customerPhone,
        String street,
        String number,
        String district,
        String city,
        String state,
        String cep,
        String latitude,
        String longitude
) { }
