package GoLogAPI.service;

import GoLogAPI.dto.DriverDTO;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.Driver;
import GoLogAPI.model.User;
import GoLogAPI.repository.DriverRepository;
import GoLogAPI.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class DriverService {

    private ModelMapper modelMapper;
    private UserRepository userRepository;
    private DriverRepository driverRepository;

    public DriverService(ModelMapper modelMapper, UserRepository userRepository ,DriverRepository driverRepository){
        this.modelMapper = modelMapper;
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
    }

    public Driver saveDriver(DriverDTO driverDTO) {
        Driver driver = modelMapper.map(driverDTO, Driver.class);
        User user = userRepository.findById(driverDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para o ID do usuario!"));
        driver.setUser(user);
        return driverRepository.save(driver);
    }

    public Driver getDriverById(int id){
        if(!driverRepository.existsById(id)){
            throw new ResourceNotFoundException("Registro não encontrado para o ID: " + id);
        }
        return driverRepository.findById(id).orElse(null);
    }

    public void deleteDriver(int id){
        if(!driverRepository.existsById(id)){
            throw new ResourceNotFoundException("Registro não encontrado para o ID: " + id);
        }
        driverRepository.deleteById(id);
    }

    public Driver updateDriver(int id, DriverDTO driverDTO){
        if(!driverRepository.existsById(id)) {
            throw new ResourceNotFoundException("Registro não encontrado para o ID: " + id);
        }
        Driver driver = modelMapper.map(driverDTO, Driver.class);
        driver.setId(id);
        return driverRepository.save(driver);
    }
}
