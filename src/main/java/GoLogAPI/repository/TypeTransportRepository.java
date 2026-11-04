package GoLogAPI.repository;

import GoLogAPI.model.TypeTransport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TypeTransportRepository extends JpaRepository<TypeTransport, UUID> {

    @Query("SELECT t FROM TypeTransport t LEFT JOIN t.company c WHERE :companyId IS NULL OR c.id IS NULL OR c.id = :companyId")
    List<TypeTransport> findAvailableForCompany(@Param("companyId") UUID companyId);
}
