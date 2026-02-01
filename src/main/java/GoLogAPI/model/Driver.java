package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "driver_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true)
    private UUID id;

    @Column(name = "cnh_number")
    private String cnhNumber;

    @Column(name = "cnh_expiration")
    private LocalDate cnhExpiration;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

}
