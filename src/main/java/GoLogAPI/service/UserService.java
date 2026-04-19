package GoLogAPI.service;

import GoLogAPI.dto.user.UserCreateRequest;
import GoLogAPI.dto.user.UserUpdateRequest;
import GoLogAPI.dto.user.UserResponse;
import GoLogAPI.dto.user.UserResponseList;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.UserMapper;
import GoLogAPI.model.Company;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.repository.UserRepository;
import GoLogAPI.model.User;
import GoLogAPI.validation.UserValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

    private UserRepository userRepository;
    private CompanyRepository companyRepository;
    private UserMapper userMapper;
    private UserValidator userValidator;
    private PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, CompanyRepository companyRepository ,UserMapper userMapper, UserValidator userValidator, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.userMapper = userMapper;
        this.userValidator = userValidator;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse save(UserCreateRequest userCreateRequest){
        Company company = companyRepository.findById(userCreateRequest.companyId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, userCreateRequest.companyId()));
        userValidator.validate(userCreateRequest);
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

    public List<UserResponseList> listAll(){
        List<User> users = userRepository.findAll();
        return userMapper.toResponses(users);
    }

    @Transactional
    public UserResponse update(UUID id, UserCreateRequest userCreateRequest){
      userRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
      userValidator.validate(userCreateRequest);
      User user = userMapper.toEntity(userCreateRequest);
      Company company = companyRepository.findById(userCreateRequest.companyId())
              .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, userCreateRequest.companyId()));
      user.setCompany(company);
      user.setId(id);
      userRepository.save(user);
      return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updatePartial(UUID id, UserUpdateRequest userUpdateRequest){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        userValidator.validate(userUpdateRequest);

        if(userUpdateRequest.name() != null) user.setName(userUpdateRequest.name());
        if(userUpdateRequest.userProfile() != null) user.setUserProfile(userUpdateRequest.userProfile());
        if(userUpdateRequest.cpf() != null) user.setCpf(userUpdateRequest.cpf());
        if(userUpdateRequest.email() != null) user.setEmail(userUpdateRequest.email());
        if(userUpdateRequest.password() != null) user.setPassword(userUpdateRequest.password());
        if(userUpdateRequest.companyId() != null){
            Company company = companyRepository.findById(userUpdateRequest.companyId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, userUpdateRequest.companyId()));
            user.setCompany(company);
        }
        userRepository.save(user);
        return userMapper.toResponse(user);
    }
}
