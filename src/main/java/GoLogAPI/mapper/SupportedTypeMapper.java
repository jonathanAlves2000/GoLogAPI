package GoLogAPI.mapper;

import GoLogAPI.dto.supportedType.SupportedTypeResponse;
import GoLogAPI.model.EquipamentGroup;
import GoLogAPI.model.TypeTransport;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {EquipamentGroupMapper.class, TypeTransportMapper.class})
public interface SupportedTypeMapper {

    SupportedTypeResponse toResponse(EquipamentGroup equipamentGroup, TypeTransport typeTransport);
}
