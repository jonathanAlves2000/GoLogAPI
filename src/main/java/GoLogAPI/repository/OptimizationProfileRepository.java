package GoLogAPI.repository;

import GoLogAPI.model.OptimizationProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OptimizationProfileRepository extends JpaRepository<OptimizationProfile, UUID> {

    List<OptimizationProfile> findByCompanyId(UUID companyId);

    Optional<OptimizationProfile> findFirstByCompanyIdAndIsDefaultTrue(UUID companyId);

    Optional<OptimizationProfile> findFirstByCompanyId(UUID companyId);
}
