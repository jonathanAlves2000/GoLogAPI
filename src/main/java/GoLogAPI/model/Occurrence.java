package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "occurrence_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE occurrence_table SET active=false WHERE id = ?")
@SQLRestriction("active = true")
public class Occurrence extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "description")
    private String description;

    @Column(name = "attachment")
    private String attachment;

    @ManyToOne
    @JoinColumn(name = "delivery_id", nullable = false)
    private Shipment shipment;

    @ManyToOne
    @JoinColumn(name = "transport_id", nullable = false)
    private Transport transport;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;
}
