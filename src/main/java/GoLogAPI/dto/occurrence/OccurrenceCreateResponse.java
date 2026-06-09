package GoLogAPI.dto.occurrence;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record OccurrenceCreateResponse(
        UUID id,
        String type,
        LocalDateTime dateTime,
        String description,
        String attachment,
        Instant createdAt,
        UUID shipmentId,
        UUID transportId,
        UUID senderId
) { }
