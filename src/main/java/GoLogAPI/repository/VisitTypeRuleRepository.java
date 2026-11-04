package GoLogAPI.repository;

import GoLogAPI.model.VisitTypeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VisitTypeRuleRepository extends JpaRepository<VisitTypeRule, UUID> {

    List<VisitTypeRule> findByCompanyId(UUID companyId);

    @Query("SELECT r FROM VisitTypeRule r LEFT JOIN r.company c WHERE :companyId IS NULL OR c.id IS NULL OR c.id = :companyId")
    List<VisitTypeRule> findAvailableByCompanyId(@Param("companyId") UUID companyId);
}
