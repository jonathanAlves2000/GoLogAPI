package GoLogAPI.dto.trailer;

import GoLogAPI.dto.equipament.EquipamentResponse;
import GoLogAPI.model.enums.EquipamentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TrailerResponse extends EquipamentResponse {

    private Double maximumVolume;
    private UUID companyId;

    public TrailerResponse(UUID id, String plate, String renavam, String model,
                           Integer numberAxles, Double maximumCapacity, Double maximumVolume, EquipamentStatus status , UUID companyId) {
        super(id, plate, renavam, model, numberAxles, maximumCapacity, status);
        this.maximumVolume = maximumVolume;
        this.companyId = companyId;
    }
}