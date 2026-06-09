package GoLogAPI.repository;

import GoLogAPI.model.RouteStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RouteStopRepository extends JpaRepository<RouteStop, UUID> {
    void deleteByTransportId(UUID transportId);

    List<RouteStop> findByTransportId(UUID transportId);
}
