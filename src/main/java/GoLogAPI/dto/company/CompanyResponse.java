package GoLogAPI.dto.company;

import GoLogAPI.dto.address.AddressResponse;
import GoLogAPI.model.enums.CompanyType;

import java.util.UUID;

public record CompanyResponse(
        UUID id,
        String legalName,
        Boolean isCliente,
        String phoneNumber,
        String email,
        String cnpjCpf,
        AddressResponse address,
        CompanyType companyType,
        Boolean isMaster,
        UUID parentCompanyId
) { }
