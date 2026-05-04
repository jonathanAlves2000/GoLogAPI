package GoLogAPI.infra.client.dto;

import java.util.List;

public record DeliveryRequest(
    String id,
    List<VisitRequest> collections,
    List<VisitRequest> deliveries
) { }
