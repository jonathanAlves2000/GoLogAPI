package GoLogAPI.service;

import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.repository.UserRepository;
import GoLogAPI.model.User;
import GoLogAPI.validation.UserValidator;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository, UserValidator userValidator){
        this.userRepository = userRepository;
    }

    public User saveUser(User user){
        return this.userRepository.save(user);
    }

    public void deleteUser(Integer id){
        if (!userRepository.existsById(id)){
            throw new ResourceNotFoundException("Registro não encontrado para o ID: " + id);
        }
        this.userRepository.deleteById(id);
    }

    public User getUserById(Integer id){
        if(!userRepository.existsById(id)){
            throw new ResourceNotFoundException("Registro não encontrado para o ID: " + id);
        }
        return this.userRepository.findById(id).orElse(null);
    }

    public User updateUser(Integer id, User user){
      if(!userRepository.existsById(id)){
          throw new ResourceNotFoundException("Registro não encontrado para o ID: " + id);
      }
        user.setId(id);
        return this.userRepository.save(user);
    }
}
