package GoLogAPI.mapper;

import GoLogAPI.dto.demandType.DemandTypeCreateRequest;
import GoLogAPI.dto.demandType.DemandTypeResponse;
import GoLogAPI.dto.demandType.DemandTypeUpdateRequest;
import GoLogAPI.model.DemandType;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DemandTypeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "isSystemDefault", ignore = true)
    DemandType toEntity(DemandTypeCreateRequest request);

    @Mapping(target = "companyId", source = "company.id")
    DemandTypeResponse toResponse(DemandType entity);

    List<DemandTypeResponse> toResponses(List<DemandType> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "isSystemDefault", ignore = true)
    void updateFromDto(DemandTypeUpdateRequest request, @MappingTarget DemandType entity);
}
