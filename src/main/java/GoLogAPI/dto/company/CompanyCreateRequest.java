package GoLogAPI.dto.company;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record CompanyDto(
        UUID id,

        @NotBlank(message = "Legal Name vazio")
        @Pattern(regexp = "^[A-Za-zÀ-ÿ ]+$", message = "Nome invalido, use somente letras ou letras com acentos")
        @Size(min = 10, max = 100, message = "Nome deve ter de 10 a 100 letras")
        String legalName,

        @NotNull(message = "IsCliente vazio")
        Boolean isCliente,

        @NotBlank(message = "Telefone vazio")
        @Pattern(regexp = "^\\([1-9]{2}\\)[0-9]{9}$", message = "Telefone inválido, formato esperado: (11)999999999")
        String phoneNumber,

        @NotBlank(message = "Email vazio")
        @Email
        String email,

        @NotBlank
        @Pattern(regexp = "(^\\d{11}$)|(^\\d{14}$)", message = "CPF ou CNPJ inválido, deve ter 11 ou 14 digitos")
        String cnpjCpf,

        @NotNull(message = "AddressId nulo")
        UUID addressId

) { }
