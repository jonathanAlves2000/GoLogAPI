package GoLogAPI.mapper;

import GoLogAPI.dto.typeTransport.TypeTransportCreateRequest;
import GoLogAPI.dto.typeTransport.TypeTransportResponse;
import GoLogAPI.model.TypeTransport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {EquipamentGroupMapper.class})
public interface TypeTransportMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "company", ignore = true)
    TypeTransport toEntity(TypeTransportCreateRequest typeTransportCreateRequest);

    @Mapping(target = "code", expression = "java(typeTransport.code())")
    @Mapping(target = "shared", expression = "java(typeTransport.getCompany() == null)")
    TypeTransportResponse toResponse(TypeTransport typeTransport);

    List<TypeTransportResponse> toResponses(List<TypeTransport> typeTransports);

}
