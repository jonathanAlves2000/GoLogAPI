package GoLogAPI.dto.occurrence;

import GoLogAPI.model.Shipment;
import GoLogAPI.model.Transport;
import GoLogAPI.model.User;

import java.time.Instant;
import java.util.UUID;

public record OccurrenceResponse(
        UUID id,
        String type,
        String description,
        Instant createdAt,
        String attachment,
        Shipment shipment,
        Transport transport,
        User sender
) { }
