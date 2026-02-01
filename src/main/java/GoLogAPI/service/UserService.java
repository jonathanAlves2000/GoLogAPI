package GoLogAPI.service;

import GoLogAPI.dto.user.UserCreateRequest;
import GoLogAPI.dto.user.UserPatchRequest;
import GoLogAPI.dto.user.UserResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.UserMapper;
import GoLogAPI.model.Company;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.repository.UserRepository;
import GoLogAPI.model.User;
import GoLogAPI.validation.UserValidator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private UserRepository userRepository;
    private CompanyRepository companyRepository;
    private UserMapper userMapper;
    private UserValidator userValidator;

    String message = "Registro não encontrado para o Id: ";

    public UserService(UserRepository userRepository, CompanyRepository companyRepository ,UserMapper userMapper, UserValidator userValidator){
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.userMapper = userMapper;
        this.userValidator = userValidator;
    }

    public UserResponse saveUser(UserCreateRequest userCreateRequest){
        userValidator.userValidate(userCreateRequest);
        User user = userMapper.toEntity(userCreateRequest);
        Company company = companyRepository.findById(userCreateRequest.companyId())
                .orElseThrow(() -> new ResourceNotFoundException(message + userCreateRequest.companyId()));
        user.setCompany(company);
        this.userRepository.save(user);
        return userMapper.toResponse(user);
    }

    public void deleteUser(UUID id){
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));
        this.userRepository.deleteById(id);
    }

    public UserResponse getUser(UUID id){
       User user = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException(message + id));
       return userMapper.toResponse(user);
    }

    public UserResponse putUser(UUID id, UserCreateRequest userCreateRequest){
      userRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException(message + id));
      //userValidator.userValidate(userDto);
      User user = userMapper.toEntity(userCreateRequest);
      Company company = companyRepository.findById(userCreateRequest.companyId())
              .orElseThrow(() -> new ResourceNotFoundException(message + userCreateRequest.companyId()));
      user.setCompany(company);
      user.setId(id);
      userRepository.save(user);
      return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse patchUser(UUID id, UserPatchRequest userPatchRequest){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));

        if(userPatchRequest.userName() != null) user.setUserName(userPatchRequest.userName());
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
