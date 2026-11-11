package GoLogAPI.dto.tractor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

import jakarta.validation.constraints.Pattern;

public record TractorUpdateRequest(

        @Pattern(regexp = "^[A-Z]{3}-?[0-9][A-Z0-9][0-9]{2}$", message = "Placa inválida")
        String plate,

        @Pattern(regexp = "^[0-9]{11}$", message = "O RENAVAM deve conter exatamente 11 dígitos numéricos")
        String renavam,

        String model,
        Integer numberAxles,
        Double maximumCapacity,
        Double costPerKilometer,
        UUID companyId,

        @Schema(description = "Categoria do veículo. Omitido = CAVALO_MECANICO (compatibilidade).")
        GoLogAPI.model.enums.VehicleType vehicleType,

        @Schema(description = "Volume útil da carroceria em m³ (veículos rígidos)")
        Double cargoVolume,

        @Schema(description = "Consumo médio em km/L. Com o combustível, define a emissão de CO2 por km.")
        Double kmPerLiter,

        @Schema(description = "Emissão em kg CO2/km medida pela operação. Se informada, prevalece sobre o cálculo por consumo.")
        Double co2PerKilometer

) implements TractorRequest { }
