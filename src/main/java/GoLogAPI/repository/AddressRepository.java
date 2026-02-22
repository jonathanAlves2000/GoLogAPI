package GoLogAPI.repository;

import GoLogAPI.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {
    boolean existsByCepAndNumber(String cep, String number);
    List<Address> findAll();
}
