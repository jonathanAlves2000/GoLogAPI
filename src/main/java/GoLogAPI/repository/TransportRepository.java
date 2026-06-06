package GoLogAPI.repository;

import GoLogAPI.model.EquipamentGroup;
import GoLogAPI.model.Transport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransportRepository extends JpaRepository<Transport, UUID > {

    Optional<Transport> findByEquipamentGroup(EquipamentGroup equipamentGroup);
}
