package GoLogAPI.repository;

import GoLogAPI.model.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, UUID> {

    Boolean existsByDriverId(UUID driverId);

    Boolean existsByEquipamentGroupId(UUID equipamentGroupId);

    List<WorkSchedule> findByEquipamentGroupId(UUID equipamentGroupId);
}
