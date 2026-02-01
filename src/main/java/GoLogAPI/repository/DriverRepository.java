package GoLogAPI.repository;

import GoLogAPI.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {

    boolean existsByCnhNumber(String cnhNumber);
}
