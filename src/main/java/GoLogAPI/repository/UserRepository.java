package GoLogAPI.repository;

import GoLogAPI.model.Driver;
import GoLogAPI.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);
    boolean existsByPassword(String password);
    boolean existsByCpf(String cpf);
//    List<Driver> findByName(String name);
}
