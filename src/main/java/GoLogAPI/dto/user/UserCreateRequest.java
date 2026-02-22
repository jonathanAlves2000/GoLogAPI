package GoLogAPI.dto.user;

import GoLogAPI.model.UserProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.util.UUID;

@Schema(name = "Create User")
public record UserCreateRequest(

        @Pattern(regexp = "^[A-Za-zÀ-ÿ ]+$", message = "Nome invalido, use somente letras ou letras com acentos")
        @Size(min = 10, max = 100, message = "Nome deve ter de 10 a 100 letras")
        String name,

        @NotBlank(message = "Email vazio")
        @Email(message = "Email invalido")
        String email,

        @NotBlank(message = "Password vazio")
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Senha invalida, use pelo menos 8 caracteres, uma letra maiúscula, uma minúscula, um número e um caractere especial")
        String password,

        @NotBlank(message = "Cpf vazio")
        @CPF
        String cpf,

        @NotNull(message = "User Profile vazio")
        @Schema(name = "User Profile")
        UserProfile userProfile,

        @NotNull(message = "CompanyId nulo")
        UUID companyId
) {}
