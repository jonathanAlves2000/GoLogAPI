package GoLogAPI.model;

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

    @Column(name = "km_per_liter", nullable = false)
    private Double kmPerLiter;

    @Column(name = "co2_per_km", nullable = false)
    private  Double co2PerKm;
}
