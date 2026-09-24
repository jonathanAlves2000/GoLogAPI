package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "equipament_capacity_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE equipament_capacity_table SET active = false WHERE id = ?")
@SQLRestriction("active = true")
public class EquipamentCapacity extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "equipament_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Equipament equipament;

    @ManyToOne(optional = false)
    @JoinColumn(name = "demand_type_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private DemandType demandType;

    @Column(name = "max_capacity", nullable = false)
    private Double maxCapacity;

    @Column(name = "soft_max_capacity")
    private Double softMaxCapacity;

    @Column(name = "cost_per_unit_above_soft_max")
    private Double costPerUnitAboveSoftMax;
}
