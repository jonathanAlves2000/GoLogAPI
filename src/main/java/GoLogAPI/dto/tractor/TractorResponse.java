package GoLogAPI.dto.tractor;

import GoLogAPI.dto.equipament.EquipamentResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TractorResponse extends EquipamentResponse {

    private String typeFuel;

    public TractorResponse(UUID id, String plate, String renavam, String model,
                           Integer namberAxles, Double maximumCapacity, String typeFuel) {
        super(id, plate, renavam, model, namberAxles, maximumCapacity);
        this.typeFuel = typeFuel;
    }
}