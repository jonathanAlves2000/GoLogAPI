package GoLogAPI.repository;

import GoLogAPI.model.Collect;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CollectRepository extends JpaRepository<Collect, UUID> {
}
