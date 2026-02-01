package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "equipament_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Equipament {

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

    @OneToOne
    @JoinColumn(name = "tractor_id", nullable = true)
    private Tractor tractor;

    @OneToOne
    @JoinColumn(name = "trailer_id", nullable = true)
    private Trailer trailer;
}
