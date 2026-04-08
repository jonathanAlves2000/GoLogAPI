package GoLogAPI.repository;

import GoLogAPI.model.EquipamentGroup;
import org.hibernate.annotations.processing.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

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
}
