package GoLogAPI.dto.company;

import GoLogAPI.dto.address.AddressResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CompanyResponse(
        UUID id,
        String legalName,
        Boolean isCliente,
        String phoneNumber,
        String email,
        String cnpjCpf,
        AddressResponse address
) { }
