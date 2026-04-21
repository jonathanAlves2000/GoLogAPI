package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.util.UUID;

@Entity
@Table(name = "transport_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE transport_table SET active=false WHERE id = ?")
@SQLRestriction("active = true")
public class Transport extends Audit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "route_return_planned", nullable = false)
    private String routeReturnPlanned;

    @Column(name = "route_return_completed", nullable = false)
    private String routeReturnCompleted;

    @Column(name = "delivery_quantity", nullable = false)
    private Integer deliveryQuantity;

    @Column(name = "total_kilometer", nullable = false)
    private Integer totalKilometer;

    @Column(name = "time_stopped", nullable = false)
    private Double timeStopped;

    @Column(name = "total_time", nullable = false)
    private Double totalTime;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "transporter_id", nullable = false)
    private Company transporter;

    @ManyToOne
    @JoinColumn(name = "equipament_group_id", nullable = false)
    private EquipamentGroup equipamentGroup;

}
