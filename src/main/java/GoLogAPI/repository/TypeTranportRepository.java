package GoLogAPI.repository;

import GoLogAPI.model.TypeTransport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface TypeTranportRepository extends JpaRepository<TypeTransport, UUID> {
}
