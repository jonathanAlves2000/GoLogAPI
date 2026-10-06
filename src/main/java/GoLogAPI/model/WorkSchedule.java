package GoLogAPI.model;

import GoLogAPI.model.enums.WorkScheduleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalTime;
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

    @ManyToOne
    @JoinColumn(name = "shift_template_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private WorkShiftTemplate shiftTemplate;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Column(name = "start_workday", nullable = false)
    private LocalTime startWorkday;

    @Column(name = "end_workday", nullable = false)
    private LocalTime endWorkday;

    // Customizações individuais da jornada (Lei do Motorista nº 13.103 / Acordos Coletivos)
    @Builder.Default
    @Column(name = "break_duration_minutes")
    private Integer breakDurationMinutes = 60;

    @Column(name = "earliest_break_time")
    private LocalTime earliestBreakTime;

    @Column(name = "latest_break_time")
    private LocalTime latestBreakTime;

    @Builder.Default
    @Column(name = "max_driving_hours_without_break")
    private Integer maxDrivingHoursWithoutBreak = 4;

    @Builder.Default
    @Column(name = "min_driving_break_minutes")
    private Integer minDrivingBreakMinutes = 30;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkScheduleStatus status;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
