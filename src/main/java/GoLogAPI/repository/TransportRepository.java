package GoLogAPI.repository;

import GoLogAPI.model.Transport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransportRepository extends JpaRepository<Transport, UUID > {
}
