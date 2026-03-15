package GoLogAPI.dto.equipament;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor // Necessário para frameworks como Jackson e Hibernate
@AllArgsConstructor // Permite o uso do super() nas classes filhas
public abstract class EquipamentResponse {

    private UUID id;
    private String plate;
    private String renavam;
    private String model;
    private Integer numberAxles;
    private Double maximumCapacity;

}