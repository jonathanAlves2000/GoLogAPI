package GoLogAPI.mapper;

import GoLogAPI.dto.equipament.EquipamentResponse;
import GoLogAPI.dto.equipamentGroup.EquipamentGroupCreateRequest;
import GoLogAPI.dto.equipamentGroup.EquipamentGroupResponse;
import GoLogAPI.model.Equipament;
import GoLogAPI.model.EquipamentGroup;
import GoLogAPI.model.Tractor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {TrailerMapper.class, TractorMapper.class})
public interface EquipamentGroupMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "equipament1", ignore = true)
    @Mapping(target = "equipament2", ignore = true)
    @Mapping(target = "equipament3", ignore = true)
    EquipamentGroup toEntity(EquipamentGroupCreateRequest equipamentGroupCreateRequest);

    EquipamentGroupResponse toResponse(EquipamentGroup equipamentGroup);
    
    default EquipamentResponse mapEquipament(Equipament equipament) {
        if (equipament == null) return null;

        if (equipament instanceof Tractor tractor) {
            return org.mapstruct.factory.Mappers.getMapper(TractorMapper.class).toResponse(tractor);
        } else if (equipament instanceof GoLogAPI.model.Trailer trailer) {
            return org.mapstruct.factory.Mappers.getMapper(TrailerMapper.class).toResponse(trailer);
        }

        return null;
    }
}
