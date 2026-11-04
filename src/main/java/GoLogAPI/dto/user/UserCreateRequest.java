package GoLogAPI.dto.user;

import GoLogAPI.model.UserProfile;
import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;
import java.util.UUID;

@Schema(name = "Create User")
public record UserCreateRequest(

        @NotBlank(message = "Nome deve ser informado")
        @Pattern(regexp = "^[A-Za-z0-9À-ÿ '’\\-.]+$", message = "Nome inválido")
        @Size(min = 2, max = 100, message = "Nome deve ter de 2 a 100 letras")
        String name,

        @NotBlank(message = "Email vazio")
        @Email(message = "Email invalido")
        String email,

        @NotBlank(message = "Password vazio")
        @Size(min = 4, max = 100, message = "A senha deve ter no mínimo 4 caracteres")
        String password,

        @NotBlank(message = "Cpf vazio")
        @Pattern(regexp = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$", message = "CPF inválido, deve conter 11 dígitos")
        String cpf,

        @NotNull(message = "User Profile vazio")
        @Schema(name = "User Profile")
        @JsonAlias({"User Profile", "userProfile"})
        UserProfile userProfile,

        @NotNull(message = "CompanyId nulo")
        UUID companyId
) implements UserRequest {}
