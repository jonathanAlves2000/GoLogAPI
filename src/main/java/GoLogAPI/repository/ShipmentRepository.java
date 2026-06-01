package GoLogAPI.repository;

import GoLogAPI.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

    @Query("SELECT SUM(s.weight) as totalWeight, SUM(s.volume) as totalVolume " +
            "FROM Shipment s WHERE s.operationOrigem.id = :operationOrigemId")
    TotalTotals findTotalsByOrigemId(@Param("operationOrigemId") UUID operationOrigemId);

    interface TotalTotals{
        Double getTotalWeight();
        Double getTotalVolume();
    }
}
