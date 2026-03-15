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

    @Column(name = "type_fuel")
    private String typeFuel;
}
