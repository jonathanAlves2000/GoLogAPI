package GoLogAPI.dto.address;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressCreateRequest(

         @NotBlank(message = "A rua deve ser informada")
         @Pattern(regexp = "^[A-Za-zÀ-ÿ\\s]+$", message = "Nome de rua invalido, utilize somente letras ou letras com acentos")
         @Size(min = 5, max = 100, message = "Nome deve ter de 10 a 100 caracteres")
         String street,

         @NotBlank(message = "O numero deve ser informado.")
         @Pattern(regexp = "^[a-z0-9]+$", message = "Numero invalido, utilize somente letras ou numeros")
         @Size(min = 1, max = 10, message = "Numero deve ter de 1 a 10 caracteres")
         String number,

         @NotBlank(message = "O bairro deve ser inforado")
         @Pattern(regexp = "^[A-Za-zÁ-ÿ\\s]+$", message = "Nome de bairro invalido, utilize somente letras ou letras com acentos")
         @Size(min = 5, max = 50, message = "Nome deve ter de 10 a 50 caracteres")
         String district,

         @NotBlank(message = "A cidade deve ser informada.")
         @Pattern(regexp = "^[A-Za-zÁ-ÿ\\s]+$", message = "Nome da cidade invalida, utilize somente letras ou letras com acentos")
         @Size(min = 5, max = 50, message = "Nome da cidade deve ter de 10 a 50 caracteres")
         String city,

         @NotBlank(message = "O estado deve ser informado")
         @Pattern(regexp = "^[A-Z]+$", message = "Estado invalido, utilize apenas a silga em Maisculo")
         @Size(min = 2, max = 2, message = "Estado deve conter apenas as siglas")
         String state,

         @NotBlank(message = "O país deve ser informado.")
         @Pattern(regexp = "^[A-Za-zÁ-ÿ\\s]+$", message = "Nome de país invalido, utilize somente letras ou letras com acentos")
         @Size(min = 5, max = 40, message = "País deve ter de 10 a 40 caracteres")
         String country,

         @NotBlank(message = "O CEP deve ser informado")
         @Pattern(regexp = "^[A-Za-z0-9]+$", message = "CEP invalido, utilize somente letras e numero")
         @Size(min = 3, max = 20, message = "CEP deve ter de 10 a 20 caracteres")
         String cep,

         @Pattern(regexp = "^[A-Za-z0-9\\s]+$", message = "Complemento invalido, utilize somente letras e numeros")
         @Size(min = 5, max = 100, message = "Complemento deve ter de 10 a 100 caracteres")
         String complement,

         @NotBlank(message = "Latitude deve ser informada.")
         @Size(min = 5, max = 100, message = "Latitude deve ter de 10 a 100 caracteres")
         String latitude,

         @NotBlank(message = "Longitude deve ser informada.")
         @Size(min = 5, max = 100, message = "Longitude deve ter de 10 a 100 caracteres")
         String longitude

) implements AddressRequest
{ }
