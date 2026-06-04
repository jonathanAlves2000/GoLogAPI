package GoLogAPI.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import GoLogAPI.dto.user.UserRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.UserRepository;

@Component
public class UserValidator {

    private final UserRepository userRepository;

    public UserValidator(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public void validate(UserRequest userRequest){
        List<String> errors = new ArrayList<>();
        if(userRequest.name() != null && !userRequest.name().isBlank())
            name(userRequest.name(), errors);

        if(userRequest.email() != null && !userRequest.email().isBlank())
            email(userRequest.email(), errors);

        if(userRequest.password() != null && !userRequest.password().isBlank())
            password(userRequest.password(), errors);

        if(userRequest.cpf() != null && !userRequest.cpf().isBlank())
            cpf(userRequest.cpf(), errors);

        if(!errors.isEmpty())
            throw new ConflictException(errors);
    }

    public void name(String userName, List<String> errors){
        boolean exists = userRepository.existsByName(userName);
        if(exists) errors.add("Nome de usuário já cadastrado!");
    }

    public void email(String userEmail, List<String> errors){
        boolean exists = userRepository.existsByEmail(userEmail);
        if(exists) errors.add("Email já cadastrado!");
    }

    public void password(String userPassword, List<String> errors){
        boolean exists = userRepository.existsByPassword(userPassword);
        if(exists) errors.add("Senha já cadastrada!");
    }

    public void cpf(String userCpf, List<String> errorList){
        boolean exists = userRepository.existsByCpf(userCpf);
        if(exists) errorList.add("CPF já cadastrado!");
    }
}
