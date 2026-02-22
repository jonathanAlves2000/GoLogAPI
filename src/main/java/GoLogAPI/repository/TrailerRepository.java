package GoLogAPI.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import GoLogAPI.model.Trailer;
import java.util.UUID;

public interface TrailerRepository extends JpaRepository<Trailer, UUID> {

}
