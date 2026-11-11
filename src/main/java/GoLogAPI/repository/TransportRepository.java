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

    Optional<Transport> findFirstByEquipamentGroupOrderByCreatedAtDesc(EquipamentGroup equipamentGroup);

    default Optional<Transport> findByEquipamentGroup(EquipamentGroup equipamentGroup) {
        return findFirstByEquipamentGroupOrderByCreatedAtDesc(equipamentGroup);
    }

    List<Transport> findByTransporterId(UUID transporterId);

    @Query("SELECT DISTINCT t FROM Transport t " +
            "LEFT JOIN t.transporter tp " +
            "LEFT JOIN t.equipamentGroup eg " +
            "LEFT JOIN eg.company egc " +
            "LEFT JOIN RouteStop rs ON rs.transport = t " +
            "LEFT JOIN rs.shipment s " +
            "LEFT JOIN s.customer sc " +
            "LEFT JOIN s.company sco " +
            "WHERE :companyId IS NULL " +
            "   OR tp.id = :companyId " +
            "   OR egc.id = :companyId " +
            "   OR sc.id = :companyId " +
            "   OR sco.id = :companyId " +
            "ORDER BY t.createdAt DESC")
    List<Transport> findVisibleForTenant(@Param("companyId") UUID companyId);

    @Query("SELECT t FROM Transport t WHERE t.equipamentGroup.equipament1.id = :equipamentId ORDER BY t.createdAt DESC")
    List<Transport> findByEquipament1IdOrderByCreatedAtDesc(@Param("equipamentId") UUID equipamentId);

    @Query("SELECT t FROM Transport t WHERE t.equipamentGroup.equipament1.plate = :plate ORDER BY t.createdAt DESC")
    List<Transport> findByEquipament1PlateOrderByCreatedAtDesc(@Param("plate") String plate);
}
