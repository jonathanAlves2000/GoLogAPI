package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Integer id;

    @Column(name = "user_name")
    private String userName;

    @Column(name= "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "cpf")
    private String cpf;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_profile")
    private UserProfile userProfile;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;
}
