package GoLogAPI.dto.company;

import java.util.UUID;

public interface CompanyRequest {
    String legalName();
    Boolean isCliente();
    String phoneNumber();
    String email();
    String cnpjCpf();
    UUID addressId();
}
