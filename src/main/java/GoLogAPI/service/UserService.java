package GoLogAPI.service;

import GoLogAPI.dto.user.UserCreateRequest;
import GoLogAPI.dto.user.UserUpdateRequest;
import GoLogAPI.dto.user.UserResponse;
import GoLogAPI.dto.user.UserResponseList;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantContext;
import GoLogAPI.mapper.UserMapper;
import GoLogAPI.model.Company;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.repository.UserRepository;
import GoLogAPI.model.User;
import GoLogAPI.validation.UserValidate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final UserMapper userMapper;
    private final UserValidate userValidate;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, CompanyRepository companyRepository , UserMapper userMapper, UserValidate userValidate, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.userMapper = userMapper;
        this.userValidate = userValidate;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse save(UserCreateRequest userCreateRequest){
        Company company = companyRepository.findById(userCreateRequest.companyId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, userCreateRequest.companyId()));
        userValidate.validate(userCreateRequest);
        String passwordEnconder = passwordEncoder.encode(userCreateRequest.password());
        User user = userMapper.toEntity(userCreateRequest);
        user.setPassword(passwordEnconder);
        user.setCompany(company);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Transactional
    public void delete(UUID id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        userRepository.delete(user);
    }

    public UserResponse get(UUID id){
       User user = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
       return userMapper.toResponse(user);
    }

    public List<UserResponseList> getAll(){
        UUID currentTenant = TenantContext.getCurrentTenantId();
        List<User> users = (currentTenant != null)
                ? userRepository.findByCompanyIdOrCompanyIsNull(currentTenant)
                : userRepository.findAll();
        return userMapper.toResponses(users);
    }

    @Transactional
    public UserResponse update(UUID id, UserUpdateRequest userUpdateRequest){
        return updatePartial(id, userUpdateRequest);
    }

    @Transactional
    public UserResponse update(UUID id, UserCreateRequest userCreateRequest){
      User existing = userRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

      if (userCreateRequest.email() != null && !userCreateRequest.email().isBlank()) {
          userRepository.findByEmail(userCreateRequest.email())
                  .filter(u -> !u.getId().equals(id))
                  .ifPresent(u -> {
                      throw new GoLogAPI.exception.ConflictException(List.of("Email já cadastrado!"));
                  });
      }
      if (userCreateRequest.cpf() != null && !userCreateRequest.cpf().isBlank()) {
          userRepository.findByCpf(userCreateRequest.cpf())
                  .filter(u -> !u.getId().equals(id))
                  .ifPresent(u -> {
                      throw new GoLogAPI.exception.ConflictException(List.of("CPF já cadastrado!"));
                  });
      }

      User user = userMapper.toEntity(userCreateRequest);
      Company company = companyRepository.findById(userCreateRequest.companyId())
              .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, userCreateRequest.companyId()));
      user.setCompany(company);
      user.setId(id);
      if (userCreateRequest.password() != null && !userCreateRequest.password().isBlank()) {
          user.setPassword(passwordEncoder.encode(userCreateRequest.password()));
      } else {
          user.setPassword(existing.getPassword());
      }
      userRepository.save(user);
      return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updatePartial(UUID id, UserUpdateRequest userUpdateRequest){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        if(userUpdateRequest.name() != null && !userUpdateRequest.name().isBlank())
            user.setName(userUpdateRequest.name());
        if(userUpdateRequest.userProfile() != null)
            user.setUserProfile(userUpdateRequest.userProfile());
        if(userUpdateRequest.cpf() != null && !userUpdateRequest.cpf().isBlank()) {
            userRepository.findByCpf(userUpdateRequest.cpf())
                    .filter(u -> !u.getId().equals(id))
                    .ifPresent(u -> {
                        throw new GoLogAPI.exception.ConflictException(List.of("CPF já cadastrado!"));
                    });
            user.setCpf(userUpdateRequest.cpf());
        }
        if(userUpdateRequest.email() != null && !userUpdateRequest.email().isBlank()) {
            userRepository.findByEmail(userUpdateRequest.email())
                    .filter(u -> !u.getId().equals(id))
                    .ifPresent(u -> {
                        throw new GoLogAPI.exception.ConflictException(List.of("Email já cadastrado!"));
                    });
            user.setEmail(userUpdateRequest.email());
        }
        if(userUpdateRequest.password() != null && !userUpdateRequest.password().isBlank())
            user.setPassword(passwordEncoder.encode(userUpdateRequest.password()));
        if(userUpdateRequest.companyId() != null){
            Company company = companyRepository.findById(userUpdateRequest.companyId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, userUpdateRequest.companyId()));
            user.setCompany(company);
        }

        userRepository.save(user);
        return userMapper.toResponse(user);
    }
}
