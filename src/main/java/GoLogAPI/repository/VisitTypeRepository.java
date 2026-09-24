package GoLogAPI.repository;

import GoLogAPI.model.VisitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VisitTypeRepository extends JpaRepository<VisitType, UUID> {

    Optional<VisitType> findByCode(String code);

    @Query("SELECT v FROM VisitType v WHERE v.company IS NULL OR v.company.id = :companyId")
    List<VisitType> findAvailableByCompanyId(@Param("companyId") UUID companyId);

    List<VisitType> findByCompanyId(UUID companyId);

    boolean existsByCodeAndCompanyId(String code, UUID companyId);
}
