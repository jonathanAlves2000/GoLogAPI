package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.UUID;

@Entity
@Table(name = "optimization_profile_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE optimization_profile_table SET active = false WHERE id = ?")
@SQLRestriction("active = true")
public class OptimizationProfile extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Company company;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    // Multiplicadores e Custos
    @Builder.Default
    @Column(name = "km_cost_multiplier", nullable = false)
    private Double kmCostMultiplier = 1.0;

    @Builder.Default
    @Column(name = "hour_cost_multiplier", nullable = false)
    private Double hourCostMultiplier = 1.0;

    @Builder.Default
    @Column(name = "fixed_cost_per_vehicle", nullable = false)
    private Double fixedCostPerVehicle = 0.0;

    @Builder.Default
    @Column(name = "cost_per_traveled_hour", nullable = false)
    private Double costPerTraveledHour = 0.0;

    @Builder.Default
    @Column(name = "penalty_cost_unserved", nullable = false)
    private Double penaltyCostUnserved = 100000.0;

    @Builder.Default
    @Column(name = "late_arrival_cost_per_hour", nullable = false)
    private Double lateArrivalCostPerHour = 0.0;

    // Tempos e Janelas Operacionais
    @Builder.Default
    @Column(name = "default_service_duration_seconds", nullable = false)
    private Integer defaultServiceDurationSeconds = 1800;

    @Builder.Default
    @Column(name = "time_window_lead_minutes", nullable = false)
    private Integer timeWindowLeadMinutes = 15;

    @Builder.Default
    @Column(name = "vehicle_start_window_lead_hours", nullable = false)
    private Integer vehicleStartWindowLeadHours = 2;

    @Builder.Default
    @Column(name = "vehicle_end_window_margin_hours", nullable = false)
    private Integer vehicleEndWindowMarginHours = 2;

    @Builder.Default
    @Column(name = "global_horizon_extra_days", nullable = false)
    private Integer globalHorizonExtraDays = 2;
}
