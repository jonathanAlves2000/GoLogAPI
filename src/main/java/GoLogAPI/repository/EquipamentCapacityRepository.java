package GoLogAPI.repository;

import GoLogAPI.model.EquipamentCapacity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EquipamentCapacityRepository extends JpaRepository<EquipamentCapacity, UUID> {

    List<EquipamentCapacity> findByEquipamentId(UUID equipamentId);

    void deleteByEquipamentId(UUID equipamentId);
}
