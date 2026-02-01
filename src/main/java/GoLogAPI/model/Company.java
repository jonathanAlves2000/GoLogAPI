package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "company_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true)
    private UUID id;

    @Column(name = "legal_name")
    private String legalName;

    @Column(name = "is_cliente")
    private Boolean isCliente;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "cnpj_cpf")
    private String cnpjCpf;

    @OneToOne
    @JoinColumn(name = "address_id")
    private Address address;

}
