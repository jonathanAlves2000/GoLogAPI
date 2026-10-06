package GoLogAPI.repository;

import GoLogAPI.model.Tractor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TractorRepository extends JpaRepository<Tractor, UUID>{
    boolean existsByPlate(String plate);
    boolean existsByRenavam(String renavam);
    List<Tractor> findByCompanyId(UUID companyId);
}
