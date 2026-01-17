package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "driver_table")
@Data
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Integer id;

    @Column(name = "cnh_number")
    private String cnhNumber;

    @Column(name = "cnh_expiration")
    private String cnhExpiration;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

}
