package GoLogAPI.repository;

import GoLogAPI.model.Company;
import GoLogAPI.model.enums.CompanyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

    boolean existsByLegalName(String legalName);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByCnpjCpf(String cnpjCpf);

    List<Company> findByCompanyTypeIn(List<CompanyType> types);
    List<Company> findByCompanyType(CompanyType companyType);
    List<Company> findByParentCompanyId(UUID parentCompanyId);
    List<Company> findByIsMasterTrue();
    Optional<Company> findFirstByIsMasterTrue();
    Optional<Company> findByCnpjCpf(String cnpjCpf);
    Optional<Company> findFirstByCnpjCpf(String cnpjCpf);

    /** Empresas visíveis a um tenant: ele mesmo, suas filiais e a carteira de clientes/fornecedores que possui. */
    @org.springframework.data.jpa.repository.Query("""
            SELECT c FROM Company c
            LEFT JOIN c.parentCompany p
            LEFT JOIN c.ownerTenant o
            WHERE c.id = :tenantId OR p.id = :tenantId OR o.id = :tenantId
            ORDER BY c.legalName
            """)
    List<Company> findVisibleForTenant(@org.springframework.data.repository.query.Param("tenantId") UUID tenantId);

    Optional<Company> findFirstByCnpjCpfAndOwnerTenantId(String cnpjCpf, UUID ownerTenantId);
}
