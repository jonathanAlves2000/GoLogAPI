package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "driver_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE driver_table SET active = false WHERE id = ?")
@Where(clause = "active = true")
public class Driver extends Audit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true)
    private UUID id;

    @Column(name = "cnh_number", nullable = false)
    private String cnhNumber;

    @Column(name = "cnh_expiration", nullable = false)
    private LocalDate cnhExpiration;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}
