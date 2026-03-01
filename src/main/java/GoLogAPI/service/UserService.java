package GoLogAPI.service;

import GoLogAPI.dto.user.UserCreateRequest;
import GoLogAPI.dto.user.UserPatchRequest;
import GoLogAPI.dto.user.UserResponse;
import GoLogAPI.dto.user.UserResponseList;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.UserMapper;
import GoLogAPI.model.Company;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.repository.UserRepository;
import GoLogAPI.model.User;
import GoLogAPI.validation.UserValidator;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private UserRepository userRepository;
    private CompanyRepository companyRepository;
    private UserMapper userMapper;
    private UserValidator userValidator;
    private PasswordEncoder passwordEncoder;

    String message = "Registro não encontrado para o Id: ";

    public UserService(UserRepository userRepository, CompanyRepository companyRepository ,UserMapper userMapper, UserValidator userValidator, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.userMapper = userMapper;
        this.userValidator = userValidator;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse save(UserCreateRequest userCreateRequest){
        Company company = companyRepository.findById(userCreateRequest.companyId())
                .orElseThrow(() -> new ResourceNotFoundException(message + userCreateRequest.companyId()));
        userValidator.validate(userCreateRequest);
        String passwordEnconder = passwordEncoder.encode(userCreateRequest.password());
        User user = userMapper.toEntity(userCreateRequest);
        user.setPassword(passwordEnconder);
        user.setCompany(company);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public void delete(UUID id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));
        userRepository.delete(user);
    }

    public UserResponse get(UUID id){
       User user = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(message + id));
       return userMapper.toResponse(user);
    }

    public List<UserResponseList> listAll(){
        List<User> users = userRepository.findAll();
        return userMapper.toResponses(users);
    }

    public UserResponse update(UUID id, UserCreateRequest userCreateRequest){
      userRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException(message + id));
      userValidator.validate(userCreateRequest);
      User user = userMapper.toEntity(userCreateRequest);
      Company company = companyRepository.findById(userCreateRequest.companyId())
              .orElseThrow(() -> new ResourceNotFoundException(message + userCreateRequest.companyId()));
      user.setCompany(company);
      user.setId(id);
      userRepository.save(user);
      return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updatePartial(UUID id, UserPatchRequest userPatchRequest){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));

        if(userPatchRequest.name() != null) user.setName(userPatchRequest.name());
        if(userPatchRequest.userProfile() != null) user.setUserProfile(userPatchRequest.userProfile());
        if(userPatchRequest.cpf() != null) user.setCpf(userPatchRequest.cpf());
        if(userPatchRequest.email() != null) user.setEmail(userPatchRequest.email());
        if(userPatchRequest.password() != null) user.setPassword(userPatchRequest.password());
        if(userPatchRequest.companyId() != null){
            Company company = companyRepository.findById(userPatchRequest.companyId())
                    .orElseThrow(() -> new ResourceNotFoundException(message + userPatchRequest.companyId()));
            user.setCompany(company);
        }
        userRepository.save(user);
        return userMapper.toResponse(user);
    }
}
