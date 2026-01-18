package GoLogAPI.service;

import GoLogAPI.dto.DriverDto;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.DriverMapper;
import GoLogAPI.model.Driver;
import GoLogAPI.model.User;
import GoLogAPI.repository.DriverRepository;
import GoLogAPI.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class DriverService {

    private DriverMapper driverMapper;
    private UserRepository userRepository;
    private DriverRepository driverRepository;

    public DriverService(DriverMapper driverMapper, UserRepository userRepository ,DriverRepository driverRepository){
        this.driverMapper = driverMapper;
        this.driverRepository = driverRepository;
        this.userRepository = userRepository;
    }

    public Driver saveDriver(DriverDto driverDto) {
        Driver driver = driverMapper.toEntity(driverDto);
        User user = userRepository.findById(driverDto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para o ID do usuario!"));
        driver.setUser(user);
        return driverRepository.save(driver);
    }

    public DriverDto getDriverById(int id){
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para o ID: " + id));
        return driverMapper.toDto(driver);
    }

    public void deleteDriver(int id){
        driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para o ID: " + id));
        driverRepository.deleteById(id);
    }

    public Driver updateDriver(int id, DriverDto driverDto){
        driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para o ID: " + id));

        Driver driver = driverMapper.toEntity(driverDto);

        User user = userRepository.findById(driverDto.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para o ID " + id));
        driver.setUser(user);

        driver.setId(id);
        return driverRepository.save(driver);
    }
}
