package GoLogAPI.mapper;

import GoLogAPI.dto.visitTypeRule.VisitTypeRuleCreateRequest;
import GoLogAPI.dto.visitTypeRule.VisitTypeRuleResponse;
import GoLogAPI.dto.visitTypeRule.VisitTypeRuleUpdateRequest;
import GoLogAPI.model.VisitTypeRule;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {VisitTypeMapper.class})
public interface VisitTypeRuleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "visitType1", ignore = true)
    @Mapping(target = "visitType2", ignore = true)
    VisitTypeRule toEntity(VisitTypeRuleCreateRequest request);

    @Mapping(target = "companyId", source = "company.id")
    VisitTypeRuleResponse toResponse(VisitTypeRule entity);

    List<VisitTypeRuleResponse> toResponses(List<VisitTypeRule> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "visitType1", ignore = true)
    @Mapping(target = "visitType2", ignore = true)
    void updateFromDto(VisitTypeRuleUpdateRequest request, @MappingTarget VisitTypeRule entity);
}
