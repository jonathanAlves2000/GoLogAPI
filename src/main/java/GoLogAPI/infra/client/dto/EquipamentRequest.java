package GoLogAPI.infra.client.dto;

import java.util.List;

public record   EquipamentRequest(
    String id,
    List<Double> startLocation,
    List<Double> endLocation,
    List<Double> loadLimits
) { }
