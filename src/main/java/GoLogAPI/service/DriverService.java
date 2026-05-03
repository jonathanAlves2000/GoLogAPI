package GoLogAPI.service;

import GoLogAPI.dto.driver.DriverCreateRequest;
import GoLogAPI.dto.driver.DriverUpdateRequest;
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
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DriverService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;
    private final DriverValidator driverValidator;

    public DriverService(UserRepository userRepository ,DriverRepository driverRepository,
                         DriverMapper driverMapper, DriverValidator driverValidator)
    {
        this.driverRepository = driverRepository;
        this.driverMapper = driverMapper;
        this.userRepository = userRepository;
        this.driverValidator = driverValidator;
    }

    @Transactional
    public DriverResponse save(DriverCreateRequest driverCreateRequest) {
        driverValidator.validate(driverCreateRequest);
        Driver driver = driverMapper.toEntity(driverCreateRequest);
        User user = userRepository.findById(driverCreateRequest.userId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, driverCreateRequest.userId()));
        driver.setUser(user);
        driverRepository.save(driver);
        return driverMapper.toResponse(driver);
    }

    public DriverResponse get(UUID id){
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return driverMapper.toResponse(driver);
    }

    public List<DriverResponseList> listAll(){
        List<Driver> drivers = driverRepository.findAll();
        return driverMapper.toResponses(drivers);
    }

    @Transactional
    public void delete(UUID id){
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        driverRepository.delete(driver);
    }

    @Transactional
    public DriverResponse update(UUID id, DriverCreateRequest driverCreateRequest){
        driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        Driver driver = driverMapper.toEntity(driverCreateRequest);
        User user = userRepository.findById(driverCreateRequest.userId())
                        .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        driver.setUser(user);
        driver.setId(id);
        driverRepository.save(driver);
        return driverMapper.toResponse(driver);
    }

    @Transactional
    public DriverResponse updatePartial(UUID id, DriverUpdateRequest driverUpdateRequest){
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        driverValidator.validate(driverUpdateRequest);

        if(driverUpdateRequest.cnhNumber() != null && !driverUpdateRequest.cnhNumber().isBlank())
            driver.setCnhNumber(driverUpdateRequest.cnhNumber());
        if(driverUpdateRequest.cnhExpiration() != null)
            driver.setCnhExpiration((driverUpdateRequest.cnhExpiration()));
        if(driverUpdateRequest.userId() != null){
            User user = userRepository.findById(driverUpdateRequest.userId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, driverUpdateRequest.userId()));
            driver.setUser(user);
        }
        driverRepository.save(driver);
        return driverMapper.toResponse(driver);
    }
}
