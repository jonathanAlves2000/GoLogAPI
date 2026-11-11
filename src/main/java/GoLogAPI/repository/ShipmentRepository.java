package GoLogAPI.repository;

import GoLogAPI.model.Shipment;
import GoLogAPI.model.enums.ShipmentStatus;
import GoLogAPI.model.enums.TypeOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    @Query("SELECT SUM(s.weight) as totalWeight, SUM(s.volume) as totalVolume " +
            "FROM Shipment s WHERE s.operationOrigem.id = :operationOrigemId")
    TotalTotals findTotalsByOrigemId(@Param("operationOrigemId") UUID operationOrigemId);

    interface TotalTotals{
        Double getTotalWeight();
        Double getTotalVolume();
    }


    List<Shipment> findByIdInAndTypeOperation(List<UUID> shipmentId, TypeOperation typeOperation);
    List<Shipment> findByOperationOrigem(Shipment shipment);

    List<Shipment> findByCompanyId(UUID companyId);
    List<Shipment> findByCompanyIdAndStatus(UUID companyId, ShipmentStatus status);

    @Query("SELECT s FROM Shipment s LEFT JOIN s.company c LEFT JOIN s.customer cust WHERE :companyId IS NULL OR c.id = :companyId OR cust.id = :companyId OR c.id IS NULL")
    List<Shipment> findByCompanyIdOrCompanyIsNull(@Param("companyId") UUID companyId);

    @Query("SELECT s FROM Shipment s LEFT JOIN s.company c LEFT JOIN s.customer cust WHERE (:companyId IS NULL OR c.id = :companyId OR cust.id = :companyId OR c.id IS NULL) AND s.status = :status")
    List<Shipment> findByCompanyIdOrCompanyIsNullAndStatus(@Param("companyId") UUID companyId, @Param("status") ShipmentStatus status);

    @Query("SELECT s, rs, t FROM Shipment s " +
            "INNER JOIN RouteStop rs ON rs.shipment = s " +
            "INNER JOIN rs.transport t " +
            "LEFT JOIN s.company sco " +
            "LEFT JOIN s.customer scust " +
            "LEFT JOIN t.transporter tp " +
            "LEFT JOIN t.equipamentGroup eg " +
            "LEFT JOIN eg.company egc " +
            "LEFT JOIN t.driver d " +
            "LEFT JOIN d.user du " +
            "WHERE s.active = true AND (:companyId IS NULL " +
            "   OR sco.id = :companyId " +
            "   OR scust.id = :companyId " +
            "   OR tp.id = :companyId " +
            "   OR egc.id = :companyId) " +
            "AND (:userId IS NULL OR du.id = :userId)")
    List<Object[]> findAllShipmentsWithRoutesAndTransportMandatoryByCompanyId(@Param("companyId") UUID companyId,
                                                                              @Param("userId") UUID userId);

    // Une as 3 tabelas (Shipment + RouteStop + Transport) de forma limpa e rápida
    @Query("SELECT s, rs, t FROM Shipment s " +
            "INNER JOIN RouteStop rs ON rs.shipment = s " +
            "INNER JOIN rs.transport t " +
            "WHERE s.active = true")
    List<Object[]> findAllShipmentsWithRoutesAndTransportMandatory();


    @Query("SELECT s " +
           "FROM Shipment s " +
           "WHERE s.status = :status")
    List<Shipment> findByStatus(@Param("status") ShipmentStatus status);


    @Query("SELECT s " +
           "FROM Shipment s " +
            "WHERE s.id = :collectId")
    Optional<Shipment> findFirstByOperationOrigemIdOrderByCreatedAtDesc(@Param("collectId") UUID collectId);

    default Shipment findByOperationOrigemId(UUID collectId) {
        return findById(collectId).orElse(null);
    }
}
