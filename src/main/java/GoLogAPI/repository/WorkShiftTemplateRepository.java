package GoLogAPI.repository;

import GoLogAPI.model.WorkShiftTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkShiftTemplateRepository extends JpaRepository<WorkShiftTemplate, UUID> {
    List<WorkShiftTemplate> findByCompanyId(UUID companyId);
    Optional<WorkShiftTemplate> findFirstByCompanyIdAndIsDefaultTrue(UUID companyId);
    List<WorkShiftTemplate> findByCompanyIdIsNull(); // Modelos globais do sistema
}
