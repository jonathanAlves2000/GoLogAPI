package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "shipment_demand_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE shipment_demand_table SET active = false WHERE id = ?")
@SQLRestriction("active = true")
public class ShipmentDemand extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shipment_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Shipment shipment;

    @ManyToOne(optional = false)
    @JoinColumn(name = "demand_type_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private DemandType demandType;

    @Column(name = "amount", nullable = false)
    private Double amount;
}
