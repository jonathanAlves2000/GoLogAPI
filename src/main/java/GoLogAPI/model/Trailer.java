package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "trailer_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE telemetry_table SET active=false WHERE id = ?")
@SQLRestriction("active = true")
public class Trailer extends Equipament {

    @Column(name = "maximum_volume", nullable = false)
    private Double maximumVolume;
}
