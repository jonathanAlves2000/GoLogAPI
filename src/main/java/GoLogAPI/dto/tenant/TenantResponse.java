package GoLogAPI.dto.tenant;

import GoLogAPI.model.enums.CompanyType;

import java.time.Instant;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String legalName,
        String cnpjCpf,
        String phoneNumber,
        String email,
        CompanyType companyType,
        Boolean isMaster,
        UUID parentCompanyId,
        String parentCompanyName,
        String city,
        String state,
        int branchCount,
        Instant createdAt,
        Boolean active
) {}
