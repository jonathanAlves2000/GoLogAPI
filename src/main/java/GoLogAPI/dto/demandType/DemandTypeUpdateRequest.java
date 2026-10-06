package GoLogAPI.dto.demandType;

public record DemandTypeUpdateRequest(
        String name,
        String unit,
        String description
) { }
