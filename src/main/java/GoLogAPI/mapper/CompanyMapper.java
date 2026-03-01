package GoLogAPI.mapper;

import GoLogAPI.dto.company.CompanyCreateRequest;
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
    Company toEntity(CompanyCreateRequest companyCreateRequest);

    CompanyResponse toResponse(Company company);

    List<CompanyResponseList> toResponses(List<Company> companies);

}
