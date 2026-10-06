package GoLogAPI.mapper;

import GoLogAPI.dto.optimizationProfile.OptimizationProfileCreateRequest;
import GoLogAPI.dto.optimizationProfile.OptimizationProfileResponse;
import GoLogAPI.dto.optimizationProfile.OptimizationProfileUpdateRequest;
import GoLogAPI.model.OptimizationProfile;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OptimizationProfileMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    OptimizationProfile toEntity(OptimizationProfileCreateRequest request);

    @Mapping(target = "companyId", source = "company.id")
    OptimizationProfileResponse toResponse(OptimizationProfile entity);

    List<OptimizationProfileResponse> toResponses(List<OptimizationProfile> entities);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    void updateFromDto(OptimizationProfileUpdateRequest request, @MappingTarget OptimizationProfile entity);
}
