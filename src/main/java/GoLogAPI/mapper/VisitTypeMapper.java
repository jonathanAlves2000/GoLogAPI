package GoLogAPI.mapper;

import GoLogAPI.dto.visitType.VisitTypeCreateRequest;
import GoLogAPI.dto.visitType.VisitTypeResponse;
import GoLogAPI.dto.visitType.VisitTypeUpdateRequest;
import GoLogAPI.model.VisitType;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VisitTypeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    VisitType toEntity(VisitTypeCreateRequest request);

    @Mapping(target = "companyId", source = "company.id")
    VisitTypeResponse toResponse(VisitType entity);

    List<VisitTypeResponse> toResponses(List<VisitType> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "company", ignore = true)
    void updateFromDto(VisitTypeUpdateRequest request, @MappingTarget VisitType entity);
}
