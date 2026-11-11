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
        @com.fasterxml.jackson.databind.annotation.JsonDeserialize(using = GoLogAPI.infra.json.FlexibleLocalDateTimeDeserializer.class)
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
        String longitude,
        /** Opcional: nome do tipo de transporte do tenant (ex.: "REFRIGERADO"). */
        String typeTransportName,
        /** Opcional: nome do tipo de carga do tenant (ex.: "Frutas"). */
        String shipmentTypeName,
        /**
         * Opcional (só ENTREGA): invoiceNumber da COLETA de origem enviada no mesmo lote. Sem ele, pares no
         * padrão "PREFIXO-COL" / "PREFIXO-ENT" são vinculados automaticamente.
         */
        String originInvoiceNumber
) { }
