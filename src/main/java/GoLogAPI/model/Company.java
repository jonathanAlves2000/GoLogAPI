package GoLogAPI.model;

import java.util.UUID;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "company_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE company_table SET active = false WHERE id = ?")
@SQLRestriction("active = true")
public class Company extends Audit{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true)
    private UUID id;

    @Column(name = "legal_name", nullable = false)
    private String legalName;

    @Column(name = "is_cliente", nullable = false)
    private Boolean isCliente;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "cnpj_cpf", nullable = false)
    private String cnpjCpf;

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"company"})
    @OneToOne
    @JoinColumn(name = "address_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    private Address address;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "company_type")
    private GoLogAPI.model.enums.CompanyType companyType;

    @Column(name = "is_master")
    private Boolean isMaster;

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"branches", "parentCompany"})
    @jakarta.persistence.ManyToOne
    @JoinColumn(name = "parent_company_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Company parentCompany;

    @com.fasterxml.jackson.annotation.JsonIgnore
    @jakarta.persistence.OneToMany(mappedBy = "parentCompany")
    private java.util.List<Company> branches;
}
