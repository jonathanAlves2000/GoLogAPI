package GoLogAPI.service;

import GoLogAPI.dto.UserDto;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.UserMapper;
import GoLogAPI.model.Company;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.repository.UserRepository;
import GoLogAPI.model.User;
import GoLogAPI.validation.UserValidator;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private UserRepository userRepository;
    private CompanyRepository companyRepository;
    private UserMapper userMapper;
    private UserValidator userValidator;

    public UserService(UserRepository userRepository, CompanyRepository companyRepository ,UserMapper userMapper, UserValidator userValidator){
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.userMapper = userMapper;
        this.userValidator = userValidator;
    }

    public UserDto saveUser(UserDto userDto){
        userValidator.userValidate(userDto);
        User user = userMapper.toEntity(userDto);
        Company company = companyRepository.findById(userDto.companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para o ID da companhia" + userDto.companyId()));
        user.setCompany(company);
        this.userRepository.save(user);
        return userMapper.toDto(user);
    }

    public void deleteUser(Integer id){
        userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para o ID: " + id));
        this.userRepository.deleteById(id);
    }

    public UserDto getUserById(Integer id){
       User user = userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para o ID: " + id));
       return userMapper.toDto(user);
    }

    public UserDto updateUser(Integer id, UserDto userDto){
      userRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para o ID: " + id));

      //userValidator.userValidate(userDto);

      User user = userMapper.toEntity(userDto);

      Company company = companyRepository.findById(userDto.companyId())
              .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para o ID da comphania" + userDto.companyId()));

      user.setCompany(company);
      user.setId(id);
      userRepository.save(user);
      return userMapper.toDto(user);
    }
}
