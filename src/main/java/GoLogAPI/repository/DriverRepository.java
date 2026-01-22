package GoLogAPI.repository;

import GoLogAPI.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Integer> {

    boolean existsByCnhNumber(String cnhNumber);
}
