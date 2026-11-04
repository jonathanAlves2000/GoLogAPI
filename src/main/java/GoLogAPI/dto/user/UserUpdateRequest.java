package GoLogAPI.dto.user;

import GoLogAPI.model.UserProfile;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import java.util.UUID;

public record UserUpdateRequest(

        @Pattern(regexp = "^$|^[A-Za-z0-9À-ÿ '’\\-.]+$", message = "Nome inválido")
        @Size(max = 100, message = "Nome deve ter até 100 letras")
        String name,

        @Email(message = "Email inválido")
        String email,

        @Pattern(regexp = "^$|^.{4,100}$", message = "A senha deve ter no mínimo 4 caracteres")
        String password,

        @Pattern(regexp = "^$|^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$", message = "CPF inválido, deve conter 11 dígitos")
        String cpf,

        @JsonAlias({"User Profile", "userProfile"})
        UserProfile userProfile,

        UUID companyId

) implements UserRequest{ }
