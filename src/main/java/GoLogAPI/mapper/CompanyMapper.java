package GoLogAPI.mapper;

import GoLogAPI.dto.company.CompanyCreateRequest;
import GoLogAPI.dto.company.CompanyCreateResponse;
import GoLogAPI.dto.company.CompanyResponse;
import GoLogAPI.dto.company.CompanyResponseList;
import GoLogAPI.model.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface CompanyMapper {

    @Mapping(target = "address", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parentCompany", ignore = true)
    @Mapping(target = "branches", ignore = true)
    @Mapping(target = "ownerTenant", ignore = true)
    @Mapping(target = "companyType", ignore = true)
    @Mapping(target = "isMaster", ignore = true)
    @Mapping(target = "webhookUrl", ignore = true)
    @Mapping(target = "webhookSecret", ignore = true)
    @Mapping(target = "webhookActive", ignore = true)
    Company toEntity(CompanyCreateRequest companyCreateRequest);

    @Mapping(source = "parentCompany.id", target = "parentCompanyId")
    CompanyResponse toResponse(Company company);

    CompanyCreateResponse toCreateResponse(Company company);

    @Mapping(source = "parentCompany.id", target = "parentCompanyId")
    @Mapping(source = "ownerTenant.id", target = "ownerTenantId")
    @Mapping(source = "ownerTenant.legalName", target = "ownerTenantName")
    @Mapping(source = "address.id", target = "addressId")
    @Mapping(source = "address.city", target = "city")
    @Mapping(source = "address.state", target = "state")
    CompanyResponseList toResponseItem(Company company);

    List<CompanyResponseList> toResponses(List<Company> companies);

}
