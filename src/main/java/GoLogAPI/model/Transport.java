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

    @Column(name = "route_return_planned", nullable = true)
    private String routeReturnPlanned;

    @Column(name = "route_return_completed", nullable = true)
    private String routeReturnCompleted;

    @Column(name = "route_planned", columnDefinition = "TEXT", nullable = false)
    private String routePlanned;

    @Column(name = "route_completed", columnDefinition = "TEXT", nullable = true)
    private String routeCompleted;

    @Column(name = "shipment_quantity", nullable = false)
    private Integer shipmentQuantity;

    @Column(name = "calculed_distance", nullable = false)
    private Integer calculedDistance;

    @Column(name = "distance_traveled", nullable = true)
    private Integer distanceTraveled;

    @Column(name = "time_stopped_calculed", nullable = false)
    private Integer timeStoppedCalculed;

    @Column(name = "time_stopped", nullable = true)
    private Integer timeStopped;

    @Column(name = "total_time_calculed")
    private Integer totalTimeCalculed;

    @Column(name = "total_time", nullable = true)
    private Integer totalTime;

    @Column(name = "total_cost_calculed", nullable = false)
    private Double totalCostCalculed;

    @Column(name = "total_cost")
    private Double totalCost;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = true)
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "transporter_id", nullable = false)
    private Company transporter;

    @ManyToOne
    @JoinColumn(name = "equipament_group_id", nullable = false)
    private EquipamentGroup equipamentGroup;

    @Column(name = "code_transport", insertable = false, updatable = false)
    @org.hibernate.annotations.Generated
    private Integer codeTransport;

}
