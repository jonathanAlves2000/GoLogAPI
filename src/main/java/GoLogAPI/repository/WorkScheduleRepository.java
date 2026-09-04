package GoLogAPI.repository;

import GoLogAPI.model.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, UUID> {
}
