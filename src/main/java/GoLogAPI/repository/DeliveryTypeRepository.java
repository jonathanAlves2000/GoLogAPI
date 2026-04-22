package GoLogAPI.repository;

import GoLogAPI.model.DeliveryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeliveryTypeRepository extends JpaRepository<DeliveryType, UUID> {
    boolean existsByName(String name);
}
