package GoLogAPI.mapper;

import GoLogAPI.dto.tractor.TractorCreateRequest;
import GoLogAPI.dto.tractor.TractorResponse;
import GoLogAPI.model.Tractor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "Spring", uses = {CompanyMapper.class})
public interface TractorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    Tractor toEntity(TractorCreateRequest tractorCreateRequest);

    TractorResponse toResponse(Tractor tractor);
}
