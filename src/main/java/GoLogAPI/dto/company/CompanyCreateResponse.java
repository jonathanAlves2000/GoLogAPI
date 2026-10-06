package GoLogAPI.dto.company;

import java.util.UUID;

public record CompanyCreateResponse (
        UUID id,
        String legalName,
        Boolean isCliente,
        String phoneNumber,
        String email,
        String cnpjCpf,
        UUID addressId
){ }
