package GoLogAPI.model;

import GoLogAPI.model.enums.WorkShiftType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "work_shift_template_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE work_shift_template_table SET active=false WHERE id = ?")
@SQLRestriction("active = true")
public class WorkShiftTemplate extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "shift_type", nullable = false)
    private WorkShiftType shiftType;

    @Column(name = "default_start_workday", nullable = false)
    private LocalTime defaultStartWorkday;

    @Column(name = "default_end_workday", nullable = false)
    private LocalTime defaultEndWorkday;

    @Builder.Default
    @Column(name = "break_duration_minutes", nullable = false)
    private Integer breakDurationMinutes = 60; // 60 min de almoço por padrão

    @Column(name = "earliest_break_time")
    private LocalTime earliestBreakTime; // Ex: 11:30

    @Column(name = "latest_break_time")
    private LocalTime latestBreakTime; // Ex: 13:30

    @Builder.Default
    @Column(name = "max_driving_hours_without_break", nullable = false)
    private Integer maxDrivingHoursWithoutBreak = 4; // Lei 13.103: máx 4h a 5h30 de direção ininterrupta

    @Builder.Default
    @Column(name = "min_driving_break_minutes", nullable = false)
    private Integer minDrivingBreakMinutes = 30; // Lei 13.103: parada mínima de 30 minutos

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @ManyToOne
    @JoinColumn(name = "company_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Company company;
}
