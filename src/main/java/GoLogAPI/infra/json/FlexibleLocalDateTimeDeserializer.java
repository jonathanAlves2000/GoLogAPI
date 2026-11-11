package GoLogAPI.infra.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/**
 * Aceita data/hora local ({@code 2026-11-12T10:00:00}) ou com fuso ({@code 2026-11-12T13:00:00Z},
 * {@code 2026-11-12T10:00:00-03:00}). Datas com fuso são convertidas para o horário de Brasília, que é o
 * horário de operação usado pelo GoLog.
 *
 * <p>Sem isto, um horário UTC enviado por integrações (ex.: {@code toIso8601ZuluString()} do Laravel) era
 * gravado como se já fosse horário local — 3 horas adiantado.</p>
 */
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    public static final ZoneId OPERATION_ZONE = ZoneId.of("America/Sao_Paulo");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getValueAsString();
        if (text == null || text.isBlank()) return null;
        String value = text.trim();
        try {
            if (value.endsWith("Z") || value.matches(".*[+-]\\d{2}:?\\d{2}$")) {
                return OffsetDateTime.parse(value).atZoneSameInstant(OPERATION_ZONE).toLocalDateTime();
            }
            return LocalDateTime.parse(value.length() == 16 ? value + ":00" : value);
        } catch (DateTimeParseException e) {
            return (LocalDateTime) ctxt.handleWeirdStringValue(LocalDateTime.class, value,
                    "use o formato ISO-8601, ex.: 2026-11-12T10:00:00 ou 2026-11-12T13:00:00Z");
        }
    }
}
