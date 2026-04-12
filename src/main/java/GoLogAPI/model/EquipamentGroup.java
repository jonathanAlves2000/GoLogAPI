package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "equipament_group_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE equipament_group_table SET active=false WHERE id = ?")
@Where(clause = "active = true")
public class EquipamentGroup extends Audit{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "observation")
    private String observation;

    @ManyToOne
    @JoinColumn(name = "equipament1_id", nullable = false)
    private Equipament equipament1;

    @ManyToOne
    @JoinColumn(name = "equipament2_id")
    private Equipament equipament2;

    @ManyToOne
    @JoinColumn(name = "equipament3_id")
    private Equipament equipament3;

    @ManyToMany
    @JoinTable(
            name = "group_transport_type_table",
            joinColumns = @JoinColumn(name = "equipament_group_id"),
            inverseJoinColumns = @JoinColumn(name = "type_transport_id")
    )
    private Set<TypeTransport> typeTransports;
}
