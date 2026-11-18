package GoLogAPI.repository;

import GoLogAPI.model.Equipament;
import GoLogAPI.model.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TelemetryRepository extends JpaRepository<Telemetry, UUID> {

    Optional<Telemetry> findTopByEquipamentIdOrderByDateTimeDesc(Equipament equipament);

    List<Telemetry> findByTransportIdIdOrderByDateTimeAsc(UUID transportId);

    List<Telemetry> findByShipmentIdIdOrderByDateTimeAsc(UUID shipmentId);

    List<Telemetry> findByTransportIdIdAndShipmentIdIdOrderByDateTimeAsc(UUID transportId, UUID shipmentId);
}
