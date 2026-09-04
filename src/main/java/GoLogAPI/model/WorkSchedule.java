package GoLogAPI.model;

import GoLogAPI.model.enums.WorkScheduleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "work_schedule")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE work_schedule SET active=false WHERE id = ?")
@SQLRestriction("active = true")
public class WorkSchedule extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "equipament_group_id", nullable = false)
    private EquipamentGroup equipamentGroup;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkScheduleStatus status;
}
