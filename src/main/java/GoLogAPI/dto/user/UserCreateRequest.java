package GoLogAPI.dto.user;

import GoLogAPI.model.UserProfile;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.util.UUID;

public record UserCreateRequest(

        @Pattern(regexp = "^[A-Za-zÀ-ÿ ]+$", message = "Nome invalido, use somente letras ou letras com acentos")
        @Size(min = 10, max = 100, message = "Nome deve ter de 10 a 100 letras")
        String userName,

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
        UserProfile userProfile,

        @NotNull(message = "CompanyId nulo")
        UUID companyId
) {}
