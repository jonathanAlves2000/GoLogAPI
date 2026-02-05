package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "trailer_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Trailer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true)
    private UUID id;

    @Column(name = "maximum_volume")
    private Double maximumVolume;
}
