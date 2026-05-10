package GoLogAPI.dto.occurrence;

import GoLogAPI.model.Delivery;
import GoLogAPI.model.Transport;
import GoLogAPI.model.User;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record OccurrenceResponse(
        UUID id,
        String type,
        String description,
        Instant createdAt,
        String attachment,
        Delivery delivery,
        Transport transport,
        User sender
) { }
