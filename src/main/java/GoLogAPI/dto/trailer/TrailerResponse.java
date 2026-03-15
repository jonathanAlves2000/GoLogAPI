package GoLogAPI.dto.trailer;

import GoLogAPI.dto.equipament.EquipamentResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TrailerResponse extends EquipamentResponse {

    private Double maximumVolume;

    public TrailerResponse(UUID id, String plate, String renavam, String model,
                           Integer namberAxles, Double maximumCapacity, Double maximumVolume) {
        super(id, plate, renavam, model, namberAxles, maximumCapacity);
        this.maximumVolume = maximumVolume;
    }
}