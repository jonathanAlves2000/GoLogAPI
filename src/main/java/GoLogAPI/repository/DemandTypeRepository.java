package GoLogAPI.repository;

import GoLogAPI.model.DemandType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DemandTypeRepository extends JpaRepository<DemandType, UUID> {

    Optional<DemandType> findByCode(String code);

    @Query("SELECT d FROM DemandType d WHERE d.isSystemDefault = true OR d.company.id = :companyId")
    List<DemandType> findAvailableByCompanyId(@Param("companyId") UUID companyId);

    List<DemandType> findByIsSystemDefaultTrue();

    boolean existsByCodeAndCompanyId(String code, UUID companyId);
}
