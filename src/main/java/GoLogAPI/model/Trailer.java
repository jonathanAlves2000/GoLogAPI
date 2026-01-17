package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "trailer_table")
@Data

public class Trailer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "maximum_volume")
    private Double maximumVolume;
}
