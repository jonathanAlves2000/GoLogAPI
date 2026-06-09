package GoLogAPI.dto.occurrence;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record OccurrenceResponseList(
        UUID id,
        LocalDateTime dateTime,
        String type,
        String description,
        Instant createdAt,
        String attachment,
        UUID shipmentId,
        UUID transportId,
        UUID senderId
) { }
