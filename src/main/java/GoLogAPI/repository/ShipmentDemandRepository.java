package GoLogAPI.repository;

import GoLogAPI.model.ShipmentDemand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShipmentDemandRepository extends JpaRepository<ShipmentDemand, UUID> {

    List<ShipmentDemand> findByShipmentId(UUID shipmentId);

    void deleteByShipmentId(UUID shipmentId);
}
