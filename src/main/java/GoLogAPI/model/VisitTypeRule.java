package GoLogAPI.model;

import GoLogAPI.model.enums.VisitTypeRuleType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "visit_type_rule_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE visit_type_rule_table SET active = false WHERE id = ?")
@SQLRestriction("active = true")
public class VisitTypeRule extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false)
    private VisitTypeRuleType ruleType;

    @ManyToOne(optional = false)
    @JoinColumn(name = "visit_type1_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private VisitType visitType1;

    @ManyToOne(optional = false)
    @JoinColumn(name = "visit_type2_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private VisitType visitType2;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Company company;
}
