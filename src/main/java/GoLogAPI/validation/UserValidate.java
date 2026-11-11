package GoLogAPI.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import GoLogAPI.dto.user.UserRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.UserRepository;

@Component
public class UserValidate {

    private final UserRepository userRepository;

    public UserValidate(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public void validate(UserRequest userRequest){
        List<String> errors = new ArrayList<>();
        if(userRequest.email() != null && !userRequest.email().isBlank())
            email(userRequest.email(), errors);

        if(userRequest.cpf() != null && !userRequest.cpf().isBlank())
            cpf(userRequest.cpf(), errors);

        if(!errors.isEmpty())
            throw new ConflictException(errors);
    }

    public void email(String userEmail, List<String> errors){
        boolean exists = userRepository.existsByEmail(userEmail);
        if(exists) errors.add("Email já cadastrado!");
    }

    public void cpf(String userCpf, List<String> errorList){
        String clean = userCpf.replaceAll("\\D", "");
        boolean exists = userRepository.existsByCpf(userCpf)
                || (!clean.isBlank() && userRepository.existsByCpf(clean));
        if(exists) errorList.add("CPF já cadastrado!");
    }
}
