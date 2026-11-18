package GoLogAPI.model;

import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "type_transport_table")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SQLDelete(sql = "UPDATE type_transport_table SET active=false WHERE id = ?")
@SQLRestriction("active = true")
@EqualsAndHashCode(callSuper = false, of = "id")

public class TypeTransport extends Audit{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    UUID id;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    String description;

    @Column(name = "care", nullable = false, columnDefinition = "text")
    String care;

    /**
     * Quando verdadeiro, só veículos/conjuntos declarados como equipados para este tipo podem atender a remessa
     * (ex.: Refrigerada exige baú refrigerado). Tipos gerais (Carga Seca) ficam falsos e qualquer veículo atende.
     */
    @Column(name = "requires_compatible_vehicle")
    Boolean requiresCompatibleVehicle = false;

    /** Código usado pelo otimizador e pelas regras de incompatibilidade: nome em maiúsculas, sem acentos, "_" no lugar de espaços. */
    public String code() {
        return codeOf(name);
    }

    public static String codeOf(String name) {
        if (name == null) return "";
        String plain = java.text.Normalizer.normalize(name.trim(), java.text.Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return plain.toUpperCase().replaceAll("[^A-Z0-9_]", "_");
    }

    @jakarta.persistence.ManyToOne
    @jakarta.persistence.JoinColumn(name = "company_id")
    @org.hibernate.annotations.NotFound(action = org.hibernate.annotations.NotFoundAction.IGNORE)
    Company company;
}
