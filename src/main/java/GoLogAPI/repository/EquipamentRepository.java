package GoLogAPI.repository;

import GoLogAPI.model.Equipament;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EquipamentRepository extends JpaRepository<Equipament, UUID> {

    Optional<Equipament> findByPlate(String plate);
    List<Equipament> findByCompanyId(UUID companyId);
}
