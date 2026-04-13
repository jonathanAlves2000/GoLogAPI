package GoLogAPI.dto.company;

import GoLogAPI.dto.address.AddressResponse;

import java.util.UUID;

public interface CompanyRequest {
    String legalName();
    Boolean isCliente();
    String phoneNumber();
    String email();
    String cnpjCpf();
    UUID addressId();
}
