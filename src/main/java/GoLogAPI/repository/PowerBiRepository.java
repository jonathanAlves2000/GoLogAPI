package GoLogAPI.repository;

import GoLogAPI.model.PowerBiToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PowerBiRepository extends JpaRepository<PowerBiToken, UUID> {

    Optional<PowerBiToken> findByJwtIdAndActiveTrue(String jwtId);
}
