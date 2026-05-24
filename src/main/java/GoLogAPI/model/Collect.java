package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "collect_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE collect_table SET active = false WHERE id = ?")
@SQLRestriction("active = true")
public class Collect extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    UUID id;

    @Column(name = "sequence", nullable = false)
    Integer sequence;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    Address collectionAddress;

    @ManyToOne
    @JoinColumn(name = "customer_collects_id", nullable = false)
    Company customerCollects;
}
