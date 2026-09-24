package GoLogAPI.model;

import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.model.enums.TypeOperation;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShipmentStatus status;

    @ManyToOne
    @JoinColumn(name = "responsible_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private User user;

    @ManyToOne
    @JoinColumn(name = "shipment_type_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private ShipmentType shipmentType;

    @ManyToOne
    @JoinColumn(name = "type_transport_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private TypeTransport typeTransport;

    @ManyToOne
    @JoinColumn(name = "shipment_address_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Address address;

    @ManyToOne
    @JoinColumn(name = "shipment_customer_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Company customer;

    @ManyToOne
    @JoinColumn(name = "carga_origem_id", nullable = true)
    @NotFound(action = NotFoundAction.IGNORE)
    private Shipment operationOrigem;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShipmentDemand> shipmentDemands = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "shipment_visit_type_table",
            joinColumns = @JoinColumn(name = "shipment_id"),
            inverseJoinColumns = @JoinColumn(name = "visit_type_id")
    )
    @Builder.Default
    private Set<VisitType> visitTypes = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "company_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Company company;
}
