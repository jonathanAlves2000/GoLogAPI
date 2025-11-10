package GoLogAPI.validation;

import GoLogAPI.model.User;
import GoLogAPI.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    private UserRepository userRepository;

    public UserValidator(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public void validatorUserName(User user){
        if(existsUserName(user.getUserName())){
            throw new IllegalArgumentException("Nome de usuário já cadastrado");
        }
    }
    private boolean existsUserName(String userName){
        return userRepository.existsByUserName(userName);
    }

    public void validatorUserEmail(User user){
        if(existsUserEmail(user.getEmail())){
            throw new IllegalArgumentException("Email já cadastrado");
        }
    }
    private boolean existsUserEmail(String userEmail){
        return userRepository.existsByEmail(userEmail);
    }
}
