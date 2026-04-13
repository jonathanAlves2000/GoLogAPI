package GoLogAPI.model;

import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "equipament_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@SQLDelete(sql = "UPDATE equipament_table SET active = false WHERE id = ?")
@SQLRestriction("active = true")
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
