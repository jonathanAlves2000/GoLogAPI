package GoLogAPI.mapper;

import GoLogAPI.dto.equipament.EquipamentResponse;
import GoLogAPI.model.Equipament;
import GoLogAPI.model.Tractor;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {TractorMapper.class, TrailerMapper.class})
public abstract class EquipamentMapper {

    @Autowired
    protected TractorMapper tractorMapper;

    @Autowired
    protected TrailerMapper trailerMapper;

    public EquipamentResponse toResponse(Equipament equipament){
        if(equipament == null) return null;

        if (equipament instanceof Tractor tractor) {
            return tractorMapper.toResponse(tractor);
        } else if (equipament instanceof GoLogAPI.model.Trailer trailer) {
            return trailerMapper.toResponse(trailer);
        }
        return null;
    }
}
