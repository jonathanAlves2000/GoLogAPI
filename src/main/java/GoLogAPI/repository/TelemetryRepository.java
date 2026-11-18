package GoLogAPI.repository;

import GoLogAPI.model.Equipament;
import GoLogAPI.model.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TelemetryRepository extends JpaRepository<Telemetry, UUID> {

    Optional<Telemetry> findTopByEquipamentIdOrderByDateTimeDesc(Equipament equipament);

    List<Telemetry> findByTransportIdIdOrderByDateTimeAsc(UUID transportId);

    @Query("select distinct t.transportId.id from Telemetry t where t.transportId is not null")
    List<UUID> findDistinctTransportIds();

    List<Telemetry> findByShipmentIdIdOrderByDateTimeAsc(UUID shipmentId);

    List<Telemetry> findByTransportIdIdAndShipmentIdIdOrderByDateTimeAsc(UUID transportId, UUID shipmentId);
}
