package SmartRouteAPI.service;

import SmartRouteAPI.repository.UserRepository;
import SmartRouteAPI.model.User;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User saveUser(User user){
        return this.userRepository.save(user);
    }

    public void deleteUser(Integer id){
        this.userRepository.deleteById(id);
    }

    public User getUserById(Integer id){
        return this.userRepository.findById(id).orElse(null);
    }

    public User updateUser(Integer id, User user){
      if(userRepository.existsById(id)){
          user.setId(id);
          return this.userRepository.save(user);
      }
        return null;
    }
}
