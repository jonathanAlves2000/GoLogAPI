package GoLogAPI.dto.tractor;

import GoLogAPI.dto.equipament.EquipamentResponse;
import GoLogAPI.model.enums.EquipamentStatus;
import GoLogAPI.model.enums.TypeFuel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TractorResponse extends EquipamentResponse {

    private TypeFuel typeFuel;
    private Double costPerKilometer;
    private UUID companyId;

    public TractorResponse(UUID id, String plate, String renavam, String model,
                           Integer numberAxles, Double maximumCapacity, TypeFuel typeFuel,
                           Double costPerKilometer, EquipamentStatus status, UUID companyId) {

        super(id, plate, renavam, model, numberAxles, maximumCapacity, status);
        this.typeFuel = typeFuel;
        this.costPerKilometer = costPerKilometer;
        this.companyId = companyId;
    }
}