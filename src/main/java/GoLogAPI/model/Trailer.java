package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trailer_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Trailer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "maximum_volume")
    private Double maximumVolume;
}
