package GoLogAPI.dto.equipament;

import java.util.UUID;

import GoLogAPI.model.enums.EquipamentStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
public abstract class EquipamentResponse {
    private UUID id;
    private String plate;
    private String renavam;
    private String model;
    private Integer numberAxles;
    private Double maximumCapacity;
    private EquipamentStatus status;
    /** Conjunto do qual o equipamento faz parte (null = ainda sem conjunto). */
    private UUID equipamentGroupId;
    /** Tipos de transporte para os quais o conjunto está equipado. */
    private java.util.List<UUID> typeTransportIds = new java.util.ArrayList<>();
    private java.util.List<String> typeTransportNames = new java.util.ArrayList<>();

    protected EquipamentResponse(UUID id, String plate, String renavam, String model, Integer numberAxles,
                                 Double maximumCapacity, EquipamentStatus status) {
        this.id = id;
        this.plate = plate;
        this.renavam = renavam;
        this.model = model;
        this.numberAxles = numberAxles;
        this.maximumCapacity = maximumCapacity;
        this.status = status;
    }
}