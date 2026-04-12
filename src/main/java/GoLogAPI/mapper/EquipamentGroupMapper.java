package GoLogAPI.mapper;

import GoLogAPI.dto.equipamentGroup.EquipamentGroupCreateRequest;
import GoLogAPI.dto.equipamentGroup.EquipamentGroupResponse;
import GoLogAPI.model.EquipamentGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {EquipamentMapper.class})
public interface EquipamentGroupMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipament1", ignore = true)
    @Mapping(target = "equipament2", ignore = true)
    @Mapping(target = "equipament3", ignore = true)
    EquipamentGroup toEntity(EquipamentGroupCreateRequest equipamentGroupCreateRequest);

    EquipamentGroupResponse toResponse(EquipamentGroup equipamentGroup);

}
