package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tractor_table")
@Data
public class Tractor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "type_fuel")
    private String typeFuel;
}
