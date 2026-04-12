package GoLogAPI.service;

import GoLogAPI.dto.equipamentGroup.EquipamentGroupCreateRequest;
import GoLogAPI.dto.equipamentGroup.EquipamentGroupPatchRequest;
import GoLogAPI.dto.equipamentGroup.EquipamentGroupResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.EquipamentGroupMapper;
import GoLogAPI.model.Equipament;
import GoLogAPI.model.EquipamentGroup;
import GoLogAPI.repository.EquipamentGroupRepository;
import GoLogAPI.repository.EquipamentRepository;
import GoLogAPI.validation.EquipamentGroupValidator;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class EquipamentGroupService {

    EquipamentGroupRepository equipamentGroupRepository;
    EquipamentGroupMapper equipamentGroupMapper;
    EquipamentRepository equipamentRepository;
    EquipamentGroupValidator equipamentGroupValidator;

    public EquipamentGroupService(EquipamentGroupRepository equipamentGroupRepository,
    EquipamentGroupMapper equipamentGroupMapper,EquipamentRepository equipamentRepository,
    EquipamentGroupValidator equipamentGroupValidator){
        this.equipamentGroupRepository = equipamentGroupRepository;
        this.equipamentGroupMapper = equipamentGroupMapper;
        this.equipamentRepository = equipamentRepository;
        this.equipamentGroupValidator= equipamentGroupValidator;
    }

    public EquipamentGroupResponse save(EquipamentGroupCreateRequest equipamentGroupCreateRequest){
        EquipamentGroup equipamentGroup = equipamentGroupMapper.toEntity(equipamentGroupCreateRequest);
        equipamentGroupValidator.validate(equipamentGroupCreateRequest);
        Equipament equipament1 = equipamentRepository.findById(equipamentGroupCreateRequest.equipament1Id())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipamentGroupCreateRequest.equipament1Id()));
        equipamentGroup.setEquipament1(equipament1);

        if(equipamentGroupCreateRequest.equipament2Id() != null) {
            Equipament equipament2 = equipamentRepository.findById(equipamentGroupCreateRequest.equipament2Id())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipamentGroupCreateRequest.equipament2Id()));
            equipamentGroup.setEquipament2(equipament2);
        }
        if(equipamentGroupCreateRequest.equipament3Id() != null){
            Equipament equipament3 = equipamentRepository.findById(equipamentGroupCreateRequest.equipament3Id())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipamentGroupCreateRequest.equipament3Id()));
            equipamentGroup.setEquipament3(equipament3);
        }
        equipamentGroupRepository.save(equipamentGroup);
        return equipamentGroupMapper.toResponse(equipamentGroup);
    }

    public EquipamentGroupResponse get(UUID id){
        EquipamentGroup equipamentGroup = equipamentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return equipamentGroupMapper.toResponse(equipamentGroup);
    }

    public void delete(UUID id){
        EquipamentGroup equipamentGroup = equipamentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        equipamentGroupRepository.delete(equipamentGroup);
    }

    public EquipamentGroupResponse update(UUID id, EquipamentGroupCreateRequest equipamentGroupCreateRequest){
        EquipamentGroup equipamentGroup = equipamentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        Equipament equipament1 = equipamentRepository.findById(equipamentGroupCreateRequest.equipament1Id())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipamentGroupCreateRequest.equipament1Id()));
        equipamentGroup.setEquipament1(equipament1);

        if(equipamentGroupCreateRequest.equipament2Id() != null) {
            Equipament equipament2 = equipamentRepository.findById(equipamentGroupCreateRequest.equipament2Id())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipamentGroupCreateRequest.equipament2Id()));
            equipamentGroup.setEquipament2(equipament2);
        }
        if(equipamentGroupCreateRequest.equipament3Id() != null){
            Equipament equipament3 = equipamentRepository.findById(equipamentGroupCreateRequest.equipament3Id())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipamentGroupCreateRequest.equipament3Id()));
            equipamentGroup.setEquipament3(equipament3);
        }

        equipamentGroupRepository.save(equipamentGroup);
        return equipamentGroupMapper.toResponse(equipamentGroup);
    }

    public EquipamentGroupResponse updatePartial(UUID id, EquipamentGroupPatchRequest equipamentGroupPatchRequest){
        EquipamentGroup equipamentGroup = equipamentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        if(equipamentGroupPatchRequest.observation() != null) equipamentGroup.setObservation(equipamentGroupPatchRequest.observation());
        if(equipamentGroupPatchRequest.equipament1Id() != null){
            Equipament equipament1 = equipamentRepository.findById(equipamentGroupPatchRequest.equipament1Id())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipamentGroupPatchRequest.equipament1Id()));
            equipamentGroup.setEquipament1(equipament1);
        }
        if(equipamentGroupPatchRequest.equipament2Id() != null){
            Equipament equipament2 = equipamentRepository.findById(equipamentGroupPatchRequest.equipament2Id())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipamentGroupPatchRequest.equipament2Id()));
            equipamentGroup.setEquipament2(equipament2);
        }
        if(equipamentGroupPatchRequest.equipament3Id() != null){
            Equipament equipament3 = equipamentRepository.findById(equipamentGroupPatchRequest.equipament3Id())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipamentGroupPatchRequest.equipament3Id()));
            equipamentGroup.setEquipament3(equipament3);
        }

        equipamentGroupRepository.save(equipamentGroup);
        return equipamentGroupMapper.toResponse(equipamentGroup);
    }
}
