package GoLogAPI.repository;

import GoLogAPI.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);
    Optional<User> findByCpf(String cpf);
    boolean existsByName(String name);
    boolean existsByEmail(String email);
    boolean existsByPassword(String password);
    boolean existsByCpf(String cpf);
    java.util.List<User> findByCompanyId(UUID companyId);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u LEFT JOIN u.company c WHERE c.id = :companyId OR c.id IS NULL")
    java.util.List<User> findByCompanyIdOrCompanyIsNull(@org.springframework.data.repository.query.Param("companyId") UUID companyId);
}
