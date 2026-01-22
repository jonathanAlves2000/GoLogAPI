package GoLogAPI.validation;

import GoLogAPI.dto.UserDto;
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

    List<String> errorList = new ArrayList<>();

    public void userValidate(UserDto userDto){

        nameValidate(userDto.userName(), errorList);
        emailValidate(userDto.email(), errorList);
        passwordValidate(userDto.password(), errorList);
        cpfValidate(userDto.cpf(), errorList);

        if(!errorList.isEmpty()){
            throw new ConflictException(errorList);
        }
    }

    public void nameValidate(String userName, List<String> errorList){
        boolean exists = userRepository.existsByUserName(userName);
        if(exists){
            errorList.add("Nome de usuário já cadastrado!");
        }
    }

    public void emailValidate(String userEmail, List<String> errorList){
        boolean exists = userRepository.existsByEmail(userEmail);
        if(exists){
            errorList.add("Email já cadastrado!");
        }
    }

    public void passwordValidate(String userPassword, List<String> errorList){
        boolean exists = userRepository.existsByPassword(userPassword);
        if(exists){
            errorList.add("Senha já cadastrada!");
        }
    }

    public void cpfValidate(String userCpf, List<String> errorList){
        boolean exists = userRepository.existsByCpf(userCpf);
        if(exists){
            errorList.add("CPF já cadastrado!");
        }
    }
}
