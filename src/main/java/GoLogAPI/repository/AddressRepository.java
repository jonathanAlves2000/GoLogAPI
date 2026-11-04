package GoLogAPI.repository;

import GoLogAPI.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
    boolean existsByCepAndNumber(String cep, String number);
    List<Address> findAll();
    List<Address> findByCompanyId(UUID companyId);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM Address a LEFT JOIN a.company c WHERE c.id = :companyId OR c.id IS NULL")
    List<Address> findByCompanyIdOrCompanyIsNull(@org.springframework.data.repository.query.Param("companyId") UUID companyId);
}
