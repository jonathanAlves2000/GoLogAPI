package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.UUID;

@Entity
@Table(name = "equipament_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@SQLDelete(sql = "UPDATE equipament_table SET active = false WHERE id = ?")
@Where(clause = "active = true")
public class Equipament extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "plate", nullable = false)
    private String plate;

    @Column(name = "renavam", nullable = false)
    private String renavam;

    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "number_axles", nullable = false)
    private Integer numberAxles;

    @Column(name = "maximum_capacity", nullable = false)
    private Double maximumCapacity;
}
