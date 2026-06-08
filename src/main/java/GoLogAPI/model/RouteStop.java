package GoLogAPI.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.util.UUID;

@Entity
@Table(name = "route_stop_table")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteStop {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "route_planned", columnDefinition = "TEXT")
    private String routePlanned;

    @Column(name = "route_completed", columnDefinition = "TEXT", nullable = true)
    private  String routeCompleted;

    @Column(name = "calculated_cost")
    private Double calculatedCost;

    @Column(name = "realized_cots", nullable = true)
    private Double realizedCots;

    @Column(name = "calculated_distance")
    private Integer calculatedDistance;

    @Column(name = "realized_distance", nullable = true)
    private Integer realizedDistance;

    @Column(name = "calculated_duration")
    private Integer calculatedDuration;

    @Column(name = "realized_duration", nullable = true)
    private Integer realizedDuration;

    @Column(name = "calculated_wait")
    private Integer calculatedWait;

    @Column(name = "realized_wait")
    private Integer realizedWait;

    @ManyToOne
    @JoinColumn(name = "transport_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Transport transport;

    @ManyToOne
    @JoinColumn(name = "shipment_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Shipment shipment;
}
