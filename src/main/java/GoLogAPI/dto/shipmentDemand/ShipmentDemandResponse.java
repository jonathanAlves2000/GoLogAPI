package GoLogAPI.dto.shipmentDemand;

import java.util.UUID;

public record ShipmentDemandResponse(
        UUID id,
        UUID demandTypeId,
        String demandTypeCode,
        String demandTypeName,
        String unit,
        Double amount
) { }
