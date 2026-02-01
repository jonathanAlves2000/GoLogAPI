package GoLogAPI.dto.user;

import GoLogAPI.model.UserProfile;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

import java.util.UUID;

public record UserPatchRequest(

        @Pattern(regexp = "^[A-Za-zÀ-ÿ ]+$", message = "Nome invalido, use somente letras ou letras com acentos")
        @Size(min = 10, max = 100, message = "Nome deve ter de 10 a 100 letras")
        String userName,

        @Email(message = "Email invalido")
        String email,

        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", message = "Senha invalida, use pelo menos 8 caracteres, uma letra maiúscula, uma minúscula, um número e um caractere especial")
        String password,

        @CPF
        String cpf,

        UserProfile userProfile,

        UUID companyId
) {
}
