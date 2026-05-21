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
    @Column(name = "id")
    private UUID id;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "volume")
    private Double volume;

    @Column(name = "scheduled_collection")
    private LocalDateTime scheduledCollection;

    @Column(name = "scheduled_delivery")
    private LocalDateTime scheduledDelivery;

    @Column(name = "route_planned", columnDefinition = "TEXT")
    private String routePlanned;

    @Column(name = "route_completed", columnDefinition = "TEXT")
    private String routeCompleted;

    @Column(name = "status")
    private String status;

    @Column(name = "delivery_sequence")
    private Integer deliverySequence;

    @ManyToOne
    @JoinColumn(name = "responsible_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "type_delivery_id")
    private DeliveryType deliveryType;

    @ManyToOne
    @JoinColumn(name = "transport_id")
    private Transport transport;

    @ManyToOne
    @JoinColumn(name = "type_transport")
    private TypeTransport typeTransport;

    @ManyToOne
    @JoinColumn(name = "origin_address_id")
    private Address originAdrress;

    @ManyToOne
    @JoinColumn(name = "destination_address_id")
    private Address destinationAddress;

    @ManyToOne
    @JoinColumn(name = "customer_collects")
    private Company customerCollects;

    @ManyToOne
    @JoinColumn(name = "customer_delivery")
    private Company customerDelivery;
}
