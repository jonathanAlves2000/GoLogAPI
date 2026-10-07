package GoLogAPI.repository;

import GoLogAPI.model.EquipamentGroup;
import GoLogAPI.model.Transport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransportRepository extends JpaRepository<Transport, UUID> {

    Optional<Transport> findByEquipamentGroup(EquipamentGroup equipamentGroup);

    List<Transport> findByTransporterId(UUID transporterId);

    @Query("SELECT t FROM Transport t WHERE t.equipamentGroup.equipament1.id = :equipamentId ORDER BY t.createdAt DESC")
    List<Transport> findByEquipament1IdOrderByCreatedAtDesc(@Param("equipamentId") UUID equipamentId);

    @Query("SELECT t FROM Transport t WHERE t.equipamentGroup.equipament1.plate = :plate ORDER BY t.createdAt DESC")
    List<Transport> findByEquipament1PlateOrderByCreatedAtDesc(@Param("plate") String plate);
}
