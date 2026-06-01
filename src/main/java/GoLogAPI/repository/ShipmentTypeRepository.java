package GoLogAPI.repository;

import GoLogAPI.model.ShipmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShipmentTypeRepository extends JpaRepository<ShipmentType, UUID> {
    boolean existsByName(String name);
}
