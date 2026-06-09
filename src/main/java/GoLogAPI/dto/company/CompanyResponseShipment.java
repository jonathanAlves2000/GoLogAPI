package GoLogAPI.dto.company;

import GoLogAPI.dto.address.AddressResponse;

import java.util.UUID;

public record CompanyResponseShipment(
        UUID id,
        String legalName,
        Boolean isCliente,
        String phoneNumber,
        String email,
        String cnpjCpf,
        UUID address
) { }
