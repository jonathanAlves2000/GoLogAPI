package GoLogAPI.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import GoLogAPI.dto.trailer.TrailerCreateRequest;
import GoLogAPI.dto.trailer.TrailerResponse;
import GoLogAPI.model.Trailer;

@Mapper(componentModel = "Spring")
public interface TrailerMapper {

    @Mapping(target = "id", ignore = true)
    Trailer toEntity(TrailerCreateRequest trailerCreateRequest);

    TrailerResponse toResponse(Trailer trailer);
}