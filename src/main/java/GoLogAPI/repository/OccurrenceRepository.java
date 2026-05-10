package GoLogAPI.repository;

import GoLogAPI.model.Occurrence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OccurrenceRepository extends JpaRepository<Occurrence, UUID> {
}
