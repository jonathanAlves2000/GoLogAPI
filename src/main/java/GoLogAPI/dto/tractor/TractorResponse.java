package GoLogAPI.dto.tractor;

import GoLogAPI.dto.equipament.EquipamentResponse;
import GoLogAPI.model.Company;
import GoLogAPI.model.TypeFuel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TractorResponse extends EquipamentResponse {

    private TypeFuel typeFuel;
    private Double kmPerLiter;
    private Double co2PerKm;
    private UUID companyId;

    public TractorResponse(UUID id, String plate, String renavam, String model,
                           Integer numberAxles, Double maximumCapacity, TypeFuel typeFuel,
                           Double kmPerLiter, Double co2PerKm, UUID companyId) {
        super(id, plate, renavam, model, numberAxles, maximumCapacity);
        this.typeFuel = typeFuel;
        this.kmPerLiter = kmPerLiter;
        this.co2PerKm = co2PerKm;
        this.companyId = companyId;
    }
}