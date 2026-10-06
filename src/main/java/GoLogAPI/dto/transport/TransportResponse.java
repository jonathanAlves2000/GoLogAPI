package GoLogAPI.dto.transport;

import java.util.UUID;

import GoLogAPI.model.Company;
import GoLogAPI.model.Driver;
import GoLogAPI.model.EquipamentGroup;

public record TransportResponse(
         UUID id,
         String routeReturnPlanned,
         String routeReturnCompleted,
         String routePlanned,
         String routeCompleted,
         Integer shipmentQuantity,
         Integer calculedDistance,
         Integer distanceTraveled,
         Integer timeStoppedCalculed,
         Integer timeStopped,
         Integer travelDuration,
         Integer totalTimeCalculed,
         Integer totalTime,
         Double costKmCalculed,
         Double costHourCalculed,
         Double totalCostCalculed,
         Double totalCost,
         Driver driver,
         Company transporter,
         EquipamentGroup equipamentGroup,
         Integer codeTransport
) { }

