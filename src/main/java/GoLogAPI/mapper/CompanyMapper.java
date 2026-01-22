package GoLogAPI.mapper;

import GoLogAPI.dto.CompanyDto;
import GoLogAPI.model.Company;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "address", ignore = true)
    Company toEntity(CompanyDto companyDto);

    @Mapping(source = "address.id", target = "addressId")
    CompanyDto toDto(Company company);
}
