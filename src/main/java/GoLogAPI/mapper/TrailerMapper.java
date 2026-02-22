package GoLogAPI.mapper;

import GoLogAPI.dto.tractor.TractorResponse;
import GoLogAPI.dto.trailer.TrailerCreateRequest;
import GoLogAPI.dto.trailer.TrailerResponse;
import org.mapstruct.Mapper;
import GoLogAPI.model.Trailer;
import org.mapstruct.Mapping;

@Mapper(componentModel = "Spring")
public interface TrailerMapper {

    @Mapping(target = "id", ignore = true)
    Trailer toEntity(TrailerCreateRequest trailerCreateRequest);

    TrailerResponse toResponse(Trailer trailer);
}
