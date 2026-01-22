package GoLogAPI.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "address_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Integer id;

    @Column(name = "street")
    private String street;

    @Column(name = "number")
    private String number;

    @Column(name = "district")
    private String district;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    private String country;

    @Column(name = "cep")
    private String cep;

    @Column(name = "complement")
    private String complement;

}
