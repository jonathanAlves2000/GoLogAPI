package GoLogAPI.repository;

import GoLogAPI.model.Equipament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipamentRepository extends JpaRepository<Equipament, UUID> {

    Optional<Equipament> findFirstByPlateOrderByCreatedAtDesc(String plate);

    Optional<Equipament> findFirstByPlateAndCompanyIdOrderByCreatedAtDesc(String plate, UUID companyId);

    List<Equipament> findAllByPlate(String plate);

    default Optional<Equipament> findByPlate(String plate) {
        return findFirstByPlateOrderByCreatedAtDesc(plate);
    }

    List<Equipament> findByCompanyId(UUID companyId);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Equipament e LEFT JOIN e.company c WHERE c.id = :companyId OR c.id IS NULL")
    List<Equipament> findByCompanyIdOrCompanyIsNull(@org.springframework.data.repository.query.Param("companyId") UUID companyId);
}
