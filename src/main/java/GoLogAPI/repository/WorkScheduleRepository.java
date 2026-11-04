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

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM WorkSchedule w WHERE w.driver.id = :driverId AND w.status IN (GoLogAPI.model.enums.WorkScheduleStatus.ATIVO, GoLogAPI.model.enums.WorkScheduleStatus.EM_OPERACAO)")
    Boolean existsActiveByDriverId(@Param("driverId") UUID driverId);

    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END FROM WorkSchedule w WHERE w.equipamentGroup.id = :equipamentGroupId AND w.status IN (GoLogAPI.model.enums.WorkScheduleStatus.ATIVO, GoLogAPI.model.enums.WorkScheduleStatus.EM_OPERACAO)")
    Boolean existsActiveByEquipamentGroupId(@Param("equipamentGroupId") UUID equipamentGroupId);

    WorkSchedule findByEquipamentGroupId(UUID equipamentGroupId);

    @Query("SELECT w " +
          "FROM WorkSchedule w " +
          "WHERE w.status = :status")
    List<WorkSchedule> findByStatus(@Param("status")WorkScheduleStatus status);

    List<WorkSchedule> findByCompanyId(UUID companyId);

    @Query("SELECT w FROM WorkSchedule w LEFT JOIN w.company c WHERE c.id = :companyId OR c.id IS NULL")
    List<WorkSchedule> findByCompanyIdOrCompanyIsNull(@Param("companyId") UUID companyId);
}
