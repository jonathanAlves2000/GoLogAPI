package GoLogAPI.dto.transport;

import java.util.UUID;

public interface TransportRequest {
    String routeReturnPlanned();
    String routeReturnCompleted();
    Integer deliveryQuantity();
    Double timeStopped();
    Double totalTime();
    UUID driverId();
    UUID transporterId();
    UUID equipamentGroupId();
}
