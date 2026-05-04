package GoLogAPI.infra.client.dto;

import java.util.List;

public record VisitRequest(
     String id,
     AddressRequest stoppingPoint,
     List<TimeWindowRequest> timeWindows
) { }
