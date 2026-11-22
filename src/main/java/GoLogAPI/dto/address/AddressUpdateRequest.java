package GoLogAPI.dto.address;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressUpdateRequest(

        @Pattern(regexp = "^[A-Za-z0-9À-ÿ\\s.,\\-'/]+$", message = "Nome de rua inválido")
        @Size(min = 2, max = 150, message = "Rua deve ter de 2 a 150 caracteres")
        String street,

        @Pattern(regexp = "^[A-Za-z0-9\\s./-]+$", message = "Número inválido, utilize apenas letras, números ou símbolos como S/N")
        @Size(min = 1, max = 20, message = "Número deve ter de 1 a 20 caracteres")
        String number,

        @Pattern(regexp = "^[A-Za-z0-9À-ÿ\\s.,\\-']+$", message = "Nome de bairro inválido")
        @Size(min = 1, max = 100, message = "Bairro deve ter de 1 a 100 caracteres")
        String district,

        @Pattern(regexp = "^[A-Za-z0-9À-ÿ\\s.,\\-']+$", message = "Nome de cidade inválido")
        @Size(min = 2, max = 100, message = "Cidade deve ter de 2 a 100 caracteres")
        String city,

        @Pattern(regexp = "^[A-Za-z]{2}$", message = "Estado inválido, utilize a sigla com 2 letras (ex: SP)")
        @Size(min = 2, max = 2, message = "Estado deve conter apenas 2 letras")
        String state,

        @Pattern(regexp = "^[A-Za-z0-9À-ÿ\\s.,\\-']+$", message = "Nome de país inválido")
        @Size(min = 2, max = 50, message = "País deve ter de 2 a 50 caracteres")
        String country,

        @Pattern(regexp = "^[A-Za-z0-9\\-.]{5,20}$", message = "CEP inválido")
        @Size(min = 5, max = 20, message = "CEP deve ter de 5 a 20 caracteres")
        String cep,

        @Size(max = 100, message = "Complemento deve ter no máximo 100 caracteres")
        String complement,

        @Size(min = 1, max = 100, message = "Latitude deve ter de 1 a 100 caracteres")
        String latitude,

        @Size(min = 1, max = 100, message = "Longitude deve ter de 1 a 100 caracteres")
        String longitude

) implements AddressRequest
{ }
