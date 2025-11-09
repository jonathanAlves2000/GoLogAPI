package SmartRouteAPI.service;

import SmartRouteAPI.repository.UserRepository;
import SmartRouteAPI.model.User;
import SmartRouteAPI.validation.UserValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;

    public UserService(UserRepository userRepository, UserValidator userValidator){
        this.userRepository = userRepository;
        this.userValidator = userValidator;
    }

    public User saveUser(User user){
        userValidator.validatorUserName(user);
        userValidator.validatorUserEmail(user);
        return this.userRepository.save(user);
    }

    public void deleteUser(Integer id){
        this.userRepository.deleteById(id);
    }

    public User getUserById(Integer id){
        return this.userRepository.findById(id).orElse(null);
    }

    public User updateUser(Integer id, User user){
      if(!userRepository.existsById(id)){
          throw new RuntimeException("Usuário com o ID " + id + "não encontrado");
      }
        user.setId(id);
        return this.userRepository.save(user);
    }
}
