package GoLogAPI.validation;

import GoLogAPI.dto.user.UserCreateRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.UserRepository;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class UserValidator {

    private UserRepository userRepository;

    public UserValidator(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    List<String> errors = new ArrayList<>();

    public void userValidate(UserCreateRequest userCreateRequest){

        nameValidate(userCreateRequest.name(), errors);
        emailValidate(userCreateRequest.email(), errors);
        passwordValidate(userCreateRequest.password(), errors);
        cpfValidate(userCreateRequest.cpf(), errors);

        if(!errors.isEmpty()){
            throw new ConflictException(errors);
        }
    }

    public void nameValidate(String userName, List<String> errors){
        boolean exists = userRepository.existsByName(userName);
        if(exists){
            errors.add("Nome de usuário já cadastrado!");
        }
    }

    public void emailValidate(String userEmail, List<String> errors){
        boolean exists = userRepository.existsByEmail(userEmail);
        if(exists){
            errors.add("Email já cadastrado!");
        }
    }

    public void passwordValidate(String userPassword, List<String> errors){
        boolean exists = userRepository.existsByPassword(userPassword);
        if(exists){
            errors.add("Senha já cadastrada!");
        }
    }

    public void cpfValidate(String userCpf, List<String> errorList){
        boolean exists = userRepository.existsByCpf(userCpf);
        if(exists){
            errorList.add("CPF já cadastrado!");
        }
    }
}
