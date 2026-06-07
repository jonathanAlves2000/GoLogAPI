package GoLogAPI.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import GoLogAPI.dto.trailer.TrailerCreateRequest;
import GoLogAPI.dto.trailer.TrailerResponse;
import GoLogAPI.model.Trailer;

import java.util.List;

@Mapper(componentModel = "Spring", uses = {CompanyMapper.class})
public interface TrailerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    Trailer toEntity(TrailerCreateRequest trailerCreateRequest);

    TrailerResponse toResponse(Trailer trailer);

    List<TrailerResponse> toResponses(List<Trailer> trailers);
}