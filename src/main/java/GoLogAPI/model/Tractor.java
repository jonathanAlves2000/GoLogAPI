package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tractor_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tractor {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true)
    private UUID id;

    @Column(name = "type_fuel")
    private String typeFuel;
}
