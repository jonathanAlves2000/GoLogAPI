package GoLogAPI.dto.equipamentCapacity;

import java.util.UUID;

public record EquipamentCapacityResponse(
        UUID id,
        UUID demandTypeId,
        String demandTypeCode,
        String demandTypeName,
        String unit,
        Double maxCapacity,
        Double softMaxCapacity,
        Double costPerUnitAboveSoftMax
) { }
