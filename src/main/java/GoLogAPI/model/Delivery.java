package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "delivery_table")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE delivery_table SET active = false WHERE id = ?")
@SQLRestriction("active = true")
public class Delivery extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "weight", nullable = false)
    private Double weight;

    @Column(name = "volume", nullable = false)
    private Double volume;

    @Column(name = "scheduled_collection", nullable = false)
    private LocalDateTime scheduledCollection;

    @Column(name = "scheduled_delivery", nullable = false)
    private LocalDateTime scheduledDelivery;

    @Column(name = "route_planned", columnDefinition = "TEXT", nullable = false)
    private String routePlanned;

    @Column(name = "route_completed", columnDefinition = "TEXT", nullable = false)
    private String routeCompleted;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "delivery_sequence", nullable = false)
    private Integer deliverySequence;

    @ManyToOne
    @JoinColumn(name = "responsible_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "type_delivery_id", nullable = false)
    private DeliveryType deliveryType;

    @ManyToOne
    @JoinColumn(name = "transport_id", nullable = false)
    private Transport transport;

    @ManyToOne
    @JoinColumn(name = "type_transport_id", nullable = false)
    private TypeTransport typeTransport;

    @ManyToOne
    @JoinColumn(name = "delivery_address_id", nullable = false)
    private Address deliveryAddress;

    @ManyToOne
    @JoinColumn(name = "customer_delivery_id", nullable = false)
    private Company customerDelivery;

    @ManyToOne
    @JoinColumn(name = "collect_id", nullable = true)
    private Collect collect;
}
