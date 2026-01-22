package GoLogAPI.service;

import GoLogAPI.dto.DriverDto;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.DriverMapper;
import GoLogAPI.model.Driver;
import GoLogAPI.model.User;
import GoLogAPI.repository.DriverRepository;
import GoLogAPI.repository.UserRepository;
import GoLogAPI.validation.DriverValidator;
import org.springframework.stereotype.Service;

@Service
public class DriverService {

    private UserRepository userRepository;
    private DriverRepository driverRepository;
    private DriverMapper driverMapper;
    private DriverValidator driverValidator;

    public DriverService(UserRepository userRepository ,DriverRepository driverRepository, DriverMapper driverMapper, DriverValidator driverValidator){
        this.driverRepository = driverRepository;
        this.driverMapper = driverMapper;
        this.userRepository = userRepository;
        this.driverValidator = driverValidator;
    }

    public DriverDto saveDriver(DriverDto driverDto) {
        driverValidator.driverValidate(driverDto);
        Driver driver = driverMapper.toEntity(driverDto);
        User user = userRepository.findById(driverDto.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para o ID do usuario!"));
        driver.setUser(user);
        driverRepository.save(driver);
        return driverMapper.toDto(driver);
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

    public DriverDto updateDriver(int id, DriverDto driverDto){
        driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para o ID: " + id));

        Driver driver = driverMapper.toEntity(driverDto);

        User user = userRepository.findById(driverDto.userId())
                        .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para o ID " + id));

        driver.setUser(user);
        driver.setId(id);
        driverRepository.save(driver);
        return driverMapper.toDto(driver);
    }
}
