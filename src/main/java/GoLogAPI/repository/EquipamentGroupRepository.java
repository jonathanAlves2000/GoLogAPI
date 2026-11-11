package GoLogAPI.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import GoLogAPI.model.EquipamentGroup;

public interface EquipamentGroupRepository extends JpaRepository<EquipamentGroup, UUID> {

    @Query(value = """
    SELECT EXISTS (
        SELECT 1 FROM tractor_table t
        JOIN equipament_table e ON t.id = e.id
        WHERE t.id = :id AND e.active = true
    )
    """, nativeQuery = true)
    boolean isTractor(@Param("id") UUID id);

    @Query("""
        SELECT COUNT(eg) > 0 
        FROM EquipamentGroup eg 
        WHERE (eg.equipament1.id = :id 
           OR eg.equipament2.id = :id 
           OR eg.equipament3.id = :id) 
        AND eg.active = true
    """)
    boolean isEquipamentUsed(@Param("id") UUID id);

    Optional<EquipamentGroup> findFirstByEquipament1IdOrderByCreatedAtDesc(UUID id);

    default Optional<EquipamentGroup> findByEquipament1Id(UUID id) {
        return findFirstByEquipament1IdOrderByCreatedAtDesc(id);
    }
    java.util.List<EquipamentGroup> findByCompanyId(UUID companyId);

    @Query("SELECT eg FROM EquipamentGroup eg LEFT JOIN eg.company c WHERE c.id = :companyId OR c.id IS NULL")
    java.util.List<EquipamentGroup> findByCompanyIdOrCompanyIsNull(@Param("companyId") UUID companyId);
}
