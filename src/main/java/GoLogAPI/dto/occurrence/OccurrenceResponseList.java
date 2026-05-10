package GoLogAPI.dto.occurrence;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record OccurrenceResponseList(
        UUID id,
        String type,
        String description,
        Instant createdAt,
        String attachment,
        UUID deliveryId,
        UUID transportId,
        UUID senderId
) { }
