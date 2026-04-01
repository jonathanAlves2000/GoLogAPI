package GoLogAPI.repository;

import GoLogAPI.model.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TelemetryRepository extends JpaRepository<Telemetry, UUID> {

}
