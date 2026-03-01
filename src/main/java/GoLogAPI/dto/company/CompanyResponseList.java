package GoLogAPI.dto.company;

import java.util.UUID;

public record CompanyResponseList(
        UUID id,
        String legalName,
        Boolean isCliente,
        String phoneNumber,
        String email,
        String cnpjCpf
) { }
