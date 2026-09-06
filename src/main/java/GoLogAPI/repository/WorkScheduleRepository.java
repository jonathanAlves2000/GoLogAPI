package GoLogAPI.repository;

import GoLogAPI.model.WorkSchedule;
import GoLogAPI.model.enums.WorkScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, UUID> {

    Boolean existsByDriverId(UUID driverId);

    Boolean existsByEquipamentGroupId(UUID equipamentGroupId);

    List<WorkSchedule> findByEquipamentGroupId(UUID equipamentGroupId);

    @Query("SELECT w " +
          "FROM WorkSchedule w " +
          "WHERE w.status = :status")
    List<WorkSchedule> findByStatus(@Param("status")WorkScheduleStatus status);
}
