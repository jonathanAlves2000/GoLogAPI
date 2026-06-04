package GoLogAPI.dto.dtoRouteOptimization.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiRouteMetrics (
        Integer travelDistanceMeters,   // Distância total que o veículo rodou
        String travelDuration,      // Tempo total dele em movimento
        String waitDuration         // Tempo total que o veículo ficou parado
){ }
