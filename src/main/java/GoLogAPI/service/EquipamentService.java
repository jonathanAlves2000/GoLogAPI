package GoLogAPI.service;

import GoLogAPI.dto.equipament.EquipamentResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.EquipamentMapper;
import GoLogAPI.model.Equipament;
import GoLogAPI.repository.EquipamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EquipamentService {

    private EquipamentRepository equipamentRepository;
    private EquipamentMapper equipamentMapper;

    public EquipamentService(EquipamentRepository equipamentRepository, EquipamentMapper equipamentMapper){
        this.equipamentRepository = equipamentRepository;
        this.equipamentMapper = equipamentMapper;
    }

    public EquipamentResponse get(UUID id){
        Equipament equipament = equipamentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return equipamentMapper.toResponse(equipament);
    }
}
