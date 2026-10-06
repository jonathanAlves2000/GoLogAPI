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
}
