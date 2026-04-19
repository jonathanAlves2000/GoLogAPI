package GoLogAPI.dto.transport;

import GoLogAPI.dto.company.CompanyResponse;
import GoLogAPI.dto.driver.DriverResponse;
import GoLogAPI.dto.equipamentGroup.EquipamentGroupResponse;

import java.util.UUID;

public record TransportResponse(
        UUID id,
        String routeReturnPlanned,
        String routeReturnCompleted,
        Integer deliveryQuantity,
        Double timeStopped,
        Double totalTime,
        DriverResponse driver,
        CompanyResponse transporter,
        EquipamentGroupResponse equipamentGroup
) { }
