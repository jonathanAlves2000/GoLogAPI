package GoLogAPI.dto.transport;

import GoLogAPI.dto.company.CompanyResponse;
import GoLogAPI.dto.driver.DriverResponse;
import GoLogAPI.dto.equipamentGroup.EquipamentGroupResponse;
import GoLogAPI.model.Company;
import GoLogAPI.model.Driver;
import GoLogAPI.model.EquipamentGroup;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.util.UUID;

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
         Integer totalTimeCalculed,
         Integer totalTime,
         Double totalCostCalculed,
         Double totalCost,
         Driver driver,
         Company transporter,
         EquipamentGroup equipamentGroup,
         Integer codeTransport
) { }

