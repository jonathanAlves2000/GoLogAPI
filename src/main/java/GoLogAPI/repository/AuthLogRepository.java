package GoLogAPI.repository;

import GoLogAPI.model.AuthLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AuthLogRepository extends JpaRepository<AuthLog, UUID> {
}
