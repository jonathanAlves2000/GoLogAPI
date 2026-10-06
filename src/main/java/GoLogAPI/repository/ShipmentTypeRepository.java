package GoLogAPI.repository;

import GoLogAPI.model.ShipmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ShipmentTypeRepository extends JpaRepository<ShipmentType, UUID> {
    boolean existsByName(String name);

    @Query("SELECT s FROM ShipmentType s WHERE :companyId IS NULL OR s.company IS NULL OR s.company.id = :companyId")
    List<ShipmentType> findAvailableForCompany(@Param("companyId") UUID companyId);
}
