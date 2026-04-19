package GoLogAPI.dto.transport;

import GoLogAPI.dto.company.CompanyResponse;
import GoLogAPI.dto.driver.DriverResponse;
import GoLogAPI.dto.equipamentGroup.EquipamentGroupResponse;

import java.util.UUID;

public record TransportCreateResponse(
        UUID id,
        String routeReturnPlanned,
        String routeReturnCompleted,
        Integer deliveryQuantity,
        Double timeStopped,
        Double totalTime,
        UUID driverId,
        UUID transporterId,
        UUID equipamentGroupId
) { }
