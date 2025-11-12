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

    @Column(name = "name")
    private String name;

    @Column(name = "cpf")
    private String cpf;

    @Column(name = "cnh")
    private String cnh;

    @Column(name = "cell_number")
    private String cellNumber;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

}
