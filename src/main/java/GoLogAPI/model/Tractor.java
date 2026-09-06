package GoLogAPI.model;

import GoLogAPI.model.enums.TypeFuel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tractor_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tractor extends Equipament{

    @Enumerated(EnumType.STRING)
    @Column(name = "type_fuel", nullable = false)
    private TypeFuel typeFuel;

    @Column(name = "cost_per_kilometer", nullable = false)
    private Double costPerKilometer;

    @Column(name = "co2_per_kilometer", nullable = false)
    private Double co2PerKilometer;

}
