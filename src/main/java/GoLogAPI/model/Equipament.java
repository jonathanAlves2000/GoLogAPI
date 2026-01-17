package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "equipament_table")
@Data
public class Equipament {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

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
