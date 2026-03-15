package GoLogAPI.repository;

import GoLogAPI.model.EquipamentGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EquipamentGroupRepository extends JpaRepository<EquipamentGroup, UUID> {

}
