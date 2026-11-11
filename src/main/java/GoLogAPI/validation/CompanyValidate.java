package GoLogAPI.validation;

import GoLogAPI.dto.company.CompanyRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.model.Company;
import GoLogAPI.repository.CompanyRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Regras de unicidade de empresas.
 *
 * <p>Tenants (transportadoras) têm CNPJ único no sistema inteiro. Clientes e fornecedores pertencem à
 * carteira de um tenant, então o mesmo CNPJ pode existir uma vez por tenant (duas transportadoras podem
 * atender a mesma Nestlé).</p>
 */
@Component
public class CompanyValidate {

    private final CompanyRepository companyRepository;

    public CompanyValidate(CompanyRepository companyRepository){
        this.companyRepository = companyRepository;
    }

    /** Validação de criação. */
    public void validate(CompanyRequest companyRequest, UUID ownerTenantId){
        validate(companyRequest, ownerTenantId, null);
    }

    /**
     * @param ownerTenantId tenant dono da carteira ({@code null} para empresas-tenant)
     * @param selfId        id da própria empresa em edições (ignorado na checagem de duplicidade)
     */
    public void validate(CompanyRequest companyRequest, UUID ownerTenantId, UUID selfId){
        List<String> errors = new ArrayList<>();

        if(companyRequest.cnpjCpf() != null && !companyRequest.cnpjCpf().isBlank())
            cnpjCpf(companyRequest.cnpjCpf(), ownerTenantId, selfId, errors);

        if(!errors.isEmpty())
            throw new ConflictException(errors);
    }

    private void cnpjCpf(String cnpjCpf, UUID ownerTenantId, UUID selfId, List<String> errors){
        String clean = cnpjCpf.replaceAll("\\D", "");
        boolean exists = companyRepository.findAll().stream()
                .filter(c -> !Objects.equals(c.getId(), selfId))
                .filter(c -> c.getCnpjCpf() != null)
                .filter(c -> c.getCnpjCpf().equals(cnpjCpf) || c.getCnpjCpf().replaceAll("\\D", "").equals(clean))
                .anyMatch(c -> sameScope(c, ownerTenantId));
        if(exists) errors.add("CNPJ ou CPF já registrado!");
    }

    private boolean sameScope(Company existing, UUID ownerTenantId) {
        UUID existingOwner = existing.getOwnerTenant() != null ? existing.getOwnerTenant().getId() : null;
        // Tenants (sem dono) colidem com qualquer cadastro; clientes só colidem dentro do mesmo tenant.
        return ownerTenantId == null || existingOwner == null || ownerTenantId.equals(existingOwner);
    }
}
