package GoLogAPI.service;

import GoLogAPI.dto.user.UserCreateRequest;
import GoLogAPI.dto.user.UserResponse;
import GoLogAPI.dto.user.UserResponseList;
import GoLogAPI.dto.user.UserUpdateRequest;
import GoLogAPI.exception.BusinessException;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantContext;
import GoLogAPI.infra.tenant.TenantGuard;
import GoLogAPI.mapper.UserMapper;
import GoLogAPI.model.Company;
import GoLogAPI.model.User;
import GoLogAPI.model.UserProfile;
import GoLogAPI.repository.UserRepository;
import GoLogAPI.validation.UserValidate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Usuários do tenant. A empresa do usuário precisa ser um tenant do escopo (a própria matriz ou uma
 * filial); clientes não possuem usuários de backoffice.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserValidate userValidate;
    private final PasswordEncoder passwordEncoder;
    private final TenantGuard tenantGuard;

    public UserService(UserRepository userRepository, UserMapper userMapper, UserValidate userValidate,
                       PasswordEncoder passwordEncoder, TenantGuard tenantGuard){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userValidate = userValidate;
        this.passwordEncoder = passwordEncoder;
        this.tenantGuard = tenantGuard;
    }

    @Transactional
    public UserResponse save(UserCreateRequest request){
        Company company = tenantGuard.ownerForWrite(request.companyId());
        userValidate.validate(request);
        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setCompany(company);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Transactional
    public void delete(UUID id){
        User user = findVisible(id);
        if (TenantContext.getUserCompanyId() != null && user.getEmail() != null
                && user.getEmail().equalsIgnoreCase(currentEmail())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Você não pode excluir o próprio usuário logado.");
        }
        userRepository.delete(user);
    }

    public UserResponse get(UUID id){
       return userMapper.toResponse(findVisible(id));
    }

    public List<UserResponseList> getAll(){
        List<User> users = tenantGuard.isGlobalView()
                ? userRepository.findAll()
                : userRepository.findByCompanyId(tenantGuard.scope());
        return userMapper.toResponses(users);
    }

    @Transactional
    public UserResponse update(UUID id, UserUpdateRequest request){
        return updatePartial(id, request);
    }

    @Transactional
    public UserResponse updatePartial(UUID id, UserUpdateRequest request){
        User user = findVisible(id);

        if(request.name() != null && !request.name().isBlank())
            user.setName(request.name());
        if(request.userProfile() != null) {
            if (request.userProfile() == UserProfile.POWERBI) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "O perfil POWERBI é reservado a tokens de integração.");
            }
            user.setUserProfile(request.userProfile());
        }
        if(request.cpf() != null && !request.cpf().isBlank()) {
            userRepository.findByCpf(request.cpf())
                    .filter(u -> !u.getId().equals(id))
                    .ifPresent(u -> { throw new ConflictException(List.of("CPF já cadastrado!")); });
            user.setCpf(request.cpf());
        }
        if(request.email() != null && !request.email().isBlank()) {
            userRepository.findByEmail(request.email())
                    .filter(u -> !u.getId().equals(id))
                    .ifPresent(u -> { throw new ConflictException(List.of("Email já cadastrado!")); });
            user.setEmail(request.email());
        }
        if(request.password() != null && !request.password().isBlank())
            user.setPassword(passwordEncoder.encode(request.password()));
        if(request.companyId() != null)
            user.setCompany(tenantGuard.ownerForWrite(request.companyId()));

        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public User findVisible(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: ", id));
        tenantGuard.checkAccess(user.getCompany(), id);
        return user;
    }

    private static String currentEmail() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof String s ? s : null;
    }
}
