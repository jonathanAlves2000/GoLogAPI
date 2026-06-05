package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shipment_table")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE shipment_table SET active = false WHERE id = ?")
@SQLRestriction("active = true")
public class Shipment extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_operation")
    private TypeOperation typeOperation;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "volume", nullable = false)
    private Double volume;

    @Column(name = "schedulind", nullable = false)
    private LocalDateTime schedulind;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "shipping_sequence", nullable = false)
    private Integer shippingSequence;

    @Column(name = "route_planned", nullable = true)
    private String routePlanned;

    @Column(name = "route_completed", nullable = true)
    private String routeCompleted;

    @Column(name = "calculated_distance", nullable = true)
    private Double calculatedDistance;

    @Column(name = "calculated_duration", nullable = true)
    private Double calculatedDuration;

    @Column(name = "calculated_wait", nullable = true)
    private Double calculatedWait;

    @Column(name = "calculated_cost", nullable = true)
    private Double calculatedCost;

    @ManyToOne
    @JoinColumn(name = "responsible_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "shipment_type_id", nullable = false)
    private ShipmentType shipmentType;

    @ManyToOne
    @JoinColumn(name = "transport_id", nullable = true)
    private Transport transport;

    @ManyToOne
    @JoinColumn(name = "type_transport_id", nullable = false)
    private TypeTransport typeTransport;

    @ManyToOne
    @JoinColumn(name = "shipment_address_id", nullable = false)
    private Address address;

    @ManyToOne
    @JoinColumn(name = "shipment_customer_id", nullable = false)
    private Company customer;

    @ManyToOne
    @JoinColumn(name = "carga_origem_id", nullable = true)
    private Shipment operationOrigem;
}
