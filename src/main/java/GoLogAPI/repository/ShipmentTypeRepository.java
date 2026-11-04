package GoLogAPI.repository;

import GoLogAPI.model.ShipmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ShipmentTypeRepository extends JpaRepository<ShipmentType, UUID> {
    boolean existsByName(String name);

    @Query("SELECT s FROM ShipmentType s LEFT JOIN s.company c WHERE :companyId IS NULL OR c.id IS NULL OR c.id = :companyId")
    List<ShipmentType> findAvailableForCompany(@Param("companyId") UUID companyId);
}
