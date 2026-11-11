package GoLogAPI.service;

import GoLogAPI.dto.driver.DriverCreateRequest;
import GoLogAPI.dto.driver.DriverResponse;
import GoLogAPI.dto.driver.DriverResponseList;
import GoLogAPI.dto.driver.DriverUpdateRequest;
import GoLogAPI.exception.BusinessException;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantGuard;
import GoLogAPI.mapper.DriverMapper;
import GoLogAPI.model.Driver;
import GoLogAPI.model.User;
import GoLogAPI.model.UserProfile;
import GoLogAPI.repository.DriverRepository;
import GoLogAPI.repository.UserRepository;
import GoLogAPI.validation.DriverValidate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Motoristas: extensão de um {@link User} com perfil DRIVER. O motorista pertence ao mesmo tenant do
 * usuário vinculado.
 */
@Service
@Transactional(readOnly = true)
public class DriverService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;
    private final DriverValidate driverValidate;
    private final TenantGuard tenantGuard;

    public DriverService(UserRepository userRepository, DriverRepository driverRepository,
                         DriverMapper driverMapper, DriverValidate driverValidate, TenantGuard tenantGuard) {
        this.driverRepository = driverRepository;
        this.driverMapper = driverMapper;
        this.userRepository = userRepository;
        this.driverValidate = driverValidate;
        this.tenantGuard = tenantGuard;
    }

    @Transactional
    public DriverResponse save(DriverCreateRequest request) {
        driverValidate.validate(request);
        User user = findDriverUser(request.userId(), null);
        Driver driver = driverMapper.toEntity(request);
        driver.setUser(user);
        driver.setCompany(user.getCompany() != null ? user.getCompany() : tenantGuard.ownerForWrite(null));
        driverRepository.save(driver);
        return driverMapper.toResponse(driver);
    }

    public DriverResponse get(UUID id){
        return driverMapper.toResponse(findVisible(id));
    }

    public List<DriverResponseList> getAll(){
        List<Driver> drivers = tenantGuard.isGlobalView()
                ? driverRepository.findAll()
                : driverRepository.findByCompanyId(tenantGuard.scope());
        return driverMapper.toResponses(drivers);
    }

    @Transactional
    public void delete(UUID id){
        driverRepository.delete(findVisible(id));
    }

    @Transactional
    public DriverResponse update(UUID id, DriverCreateRequest request){
        Driver driver = findVisible(id);
        driverValidate.validate(request, id);
        User user = findDriverUser(request.userId(), id);
        driver.setCnhNumber(request.cnhNumber());
        driver.setCnhExpiration(request.cnhExpiration());
        driver.setCostPerHour(request.costPerHour());
        driver.setUser(user);
        if (user.getCompany() != null) driver.setCompany(user.getCompany());
        driverRepository.save(driver);
        return driverMapper.toResponse(driver);
    }

    @Transactional
    public DriverResponse updatePartial(UUID id, DriverUpdateRequest request){
        Driver driver = findVisible(id);
        driverValidate.validate(request, id);

        if(request.cnhNumber() != null && !request.cnhNumber().isBlank())
            driver.setCnhNumber(request.cnhNumber());
        if(request.cnhExpiration() != null)
            driver.setCnhExpiration(request.cnhExpiration());
        if(request.costPerHour() != null)
            driver.setCostPerHour(request.costPerHour());
        if(request.userId() != null){
            User user = findDriverUser(request.userId(), id);
            driver.setUser(user);
            if (user.getCompany() != null) driver.setCompany(user.getCompany());
        }
        driverRepository.save(driver);
        return driverMapper.toResponse(driver);
    }

    public Driver findVisible(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorista não encontrado: ", id));
        tenantGuard.checkAccess(driver.getCompany(), id);
        return driver;
    }

    /** Usuário a vincular: precisa existir no escopo, ter perfil DRIVER e não ser motorista de outro cadastro. */
    private User findDriverUser(UUID userId, UUID selfDriverId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: ", userId));
        tenantGuard.checkAccess(user.getCompany(), userId);
        if (user.getUserProfile() != UserProfile.DRIVER) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "O usuário '" + user.getName() + "' não tem perfil Motorista. Altere o perfil do usuário antes de cadastrá-lo como motorista.");
        }
        boolean alreadyDriver = driverRepository.findAll().stream()
                .anyMatch(d -> d.getUser() != null && d.getUser().getId().equals(userId) && !d.getId().equals(selfDriverId));
        if (alreadyDriver) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "O usuário '" + user.getName() + "' já está cadastrado como motorista.");
        }
        return user;
    }
}
