package GoLogAPI.service;

import GoLogAPI.dto.driver.DriverCreateRequest;
import GoLogAPI.dto.driver.DriverPatchRequest;
import GoLogAPI.dto.driver.DriverResponseList;
import GoLogAPI.dto.driver.DriverResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.DriverMapper;
import GoLogAPI.model.Driver;
import GoLogAPI.model.User;
import GoLogAPI.repository.DriverRepository;
import GoLogAPI.repository.UserRepository;
import GoLogAPI.validation.DriverValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class DriverService {

    private UserRepository userRepository;
    private DriverRepository driverRepository;
    private DriverMapper driverMapper;
    private DriverValidator driverValidator;

    String message = "Registro não encontrado para o Id: ";

    public DriverService(UserRepository userRepository ,DriverRepository driverRepository, DriverMapper driverMapper, DriverValidator driverValidator){
        this.driverRepository = driverRepository;
        this.driverMapper = driverMapper;
        this.userRepository = userRepository;
        this.driverValidator = driverValidator;
    }

    public DriverResponse save(DriverCreateRequest driverCreateRequest) {
        driverValidator.validate(driverCreateRequest);
        Driver driver = driverMapper.toEntity(driverCreateRequest);
        User user = userRepository.findById(driverCreateRequest.userId())
                .orElseThrow(() -> new ResourceNotFoundException(message + driverCreateRequest.userId()));
        driver.setUser(user);
        driverRepository.save(driver);
        return driverMapper.toResponse(driver);
    }

    public DriverResponse get(UUID id){
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));
        return driverMapper.toResponse(driver);
    }

    public List<DriverResponseList> listAll(){
        List<Driver> drivers = driverRepository.findAll();
        return driverMapper.toResponses(drivers);
    }

    public void delete(UUID id){
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));
        driverRepository.delete(driver);
    }

    public DriverResponse update(UUID id, DriverCreateRequest driverCreateRequest){
        driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));
        Driver driver = driverMapper.toEntity(driverCreateRequest);
        User user = userRepository.findById(driverCreateRequest.userId())
                        .orElseThrow(() -> new ResourceNotFoundException(message + id));
        driver.setUser(user);
        driver.setId(id);
        driverRepository.save(driver);
        return driverMapper.toResponse(driver);
    }

    public DriverResponse updatePartial(UUID id, DriverPatchRequest driverPatchRequest){
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));

        if(driverPatchRequest.cnhNumber() != null) driver.setCnhNumber(driverPatchRequest.cnhNumber());
        if(driverPatchRequest.cnhExpiration() != null) driver.setCnhExpiration((driverPatchRequest.cnhExpiration()));
        if(driverPatchRequest.userId() != null){
            User user = userRepository.findById(driverPatchRequest.userId())
                    .orElseThrow(() -> new ResourceNotFoundException(message + driverPatchRequest.userId()));
            driver.setUser(user);
        }
        driverRepository.save(driver);
        return driverMapper.toResponse(driver);
    }
}
