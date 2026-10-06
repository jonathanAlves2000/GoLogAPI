package GoLogAPI.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import GoLogAPI.model.Trailer;
import java.util.List;
import java.util.UUID;

public interface TrailerRepository extends JpaRepository<Trailer, UUID> {
    boolean existsByPlate(String plate);
    boolean existsByRenavam(String renavam);
    List<Trailer> findByCompanyId(UUID companyId);
}
