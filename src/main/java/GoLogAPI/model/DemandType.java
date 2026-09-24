package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "demand_type_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE demand_type_table SET active = false WHERE id = ?")
@SQLRestriction("active = true")
public class DemandType extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "description")
    private String description;

    @Builder.Default
    @Column(name = "is_system_default", nullable = false)
    private Boolean isSystemDefault = false;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Company company;
}
