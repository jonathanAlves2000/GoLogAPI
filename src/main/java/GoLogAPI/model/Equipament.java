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
    @Column(name = "id")
    private UUID id;

    @Column(name = "plate")
    private String plate;

    @Column(name = "renavam")
    private String renavam;

    @Column(name = "model")
    private String model;

    @Column(name = "namber_axles")
    private Integer namberAxles;

    @Column(name = "maximum_capacity")
    private Double maximumCapacity;
}
