package GoLogAPI.dto.tenant;

import GoLogAPI.model.enums.CompanyType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TenantCreateRequest(
        @NotBlank(message = "Razão social / Nome do Tenant é obrigatório")
        String legalName,

        @NotBlank(message = "CNPJ ou CPF é obrigatório")
        String cnpjCpf,

        @NotBlank(message = "Telefone é obrigatório")
        String phoneNumber,

        @NotBlank(message = "E-mail corporativo é obrigatório")
        @Email(message = "E-mail inválido")
        String email,

        @NotNull(message = "Tipo de empresa é obrigatório")
        CompanyType companyType,

        UUID parentCompanyId,

        @NotBlank(message = "CEP é obrigatório")
        String cep,

        @NotBlank(message = "Logradouro é obrigatório")
        String street,

        @NotBlank(message = "Número é obrigatório")
        String number,

        String district,
        String city,
        String state,
        String complement,
        String latitude,
        String longitude,

        @NotBlank(message = "Nome do Administrador é obrigatório")
        String adminName,

        @NotBlank(message = "E-mail do Administrador é obrigatório")
        @Email(message = "E-mail do administrador inválido")
        String adminEmail,

        @NotBlank(message = "Senha do Administrador é obrigatória")
        String adminPassword,

        @NotBlank(message = "CPF do Administrador é obrigatório")
        String adminCpf
) {}
