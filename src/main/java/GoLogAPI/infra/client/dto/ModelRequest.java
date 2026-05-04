package GoLogAPI.infra.client.dto;

import java.util.List;

public record ModelRequest(
    List<DeliveryRequest> deliveries,
    List<EquipamentRequest> equipaments
) { }
