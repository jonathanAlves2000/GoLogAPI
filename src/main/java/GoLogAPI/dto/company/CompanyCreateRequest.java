package GoLogAPI.dto.company;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record CompanyCreateRequest(

        @NotBlank(message = "Razão social / Nome deve ser informado")
        @Pattern(regexp = "^[A-Za-z0-9À-ÿ\\s.,/\\-&'’()#@+]+$", message = "Nome inválido")
        @Size(min = 2, max = 150, message = "Nome deve ter de 2 a 150 caracteres")
        String legalName,

        @NotNull(message = "Status de cliente vazio")
        Boolean isCliente,

        @NotBlank(message = "Telefone vazio")
        @Pattern(regexp = "^\\+?[0-9()\\s\\-.]{8,20}$",
                message = "Telefone inválido, formato esperado: (11)999999999 ou (11)33333333")
        String phoneNumber,

        @NotBlank(message = "Email vazio")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "CPF ou CNPJ é obrigatório")
        @Pattern(regexp = "^(\\d{11}|\\d{14}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}|\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2})$", message = "CPF ou CNPJ inválido, deve ter 11 ou 14 dígitos")
        String cnpjCpf,

        @NotNull(message = "AddressId nulo")
        UUID addressId

) implements CompanyRequest { }
