package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tractor_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tractor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "type_fuel")
    private String typeFuel;
}
