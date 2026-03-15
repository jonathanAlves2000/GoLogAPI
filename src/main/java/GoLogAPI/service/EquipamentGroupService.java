package GoLogAPI.service;

import GoLogAPI.dto.equipamentGroup.EquipamentGroupCreateRequest;
import GoLogAPI.dto.equipamentGroup.EquipamentGroupResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.EquipamentGroupMapper;
import GoLogAPI.model.Equipament;
import GoLogAPI.model.EquipamentGroup;
import GoLogAPI.repository.EquipamentGroupRepository;
import GoLogAPI.repository.EquipamentRepository;
import org.springframework.stereotype.Service;

@Service
public class EquipamentGroupService {

    EquipamentGroupRepository equipamentGroupRepository;
    EquipamentGroupMapper equipamentGroupMapper;
    EquipamentRepository equipamentRepository;

    public EquipamentGroupService(EquipamentGroupRepository equipamentGroupRepository,
    EquipamentGroupMapper equipamentGroupMapper,EquipamentRepository equipamentRepository){
        this.equipamentGroupRepository = equipamentGroupRepository;
        this.equipamentGroupMapper = equipamentGroupMapper;
        this.equipamentRepository = equipamentRepository;
    }

    public EquipamentGroupResponse save(EquipamentGroupCreateRequest equipamentGroupCreateRequest){
        EquipamentGroup equipamentGroup = equipamentGroupMapper.toEntity(equipamentGroupCreateRequest);

        Equipament equipament1 = equipamentRepository.findById(equipamentGroupCreateRequest.equipamentId1())
                .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.NOT_FOUND_MESSAGE + equipamentGroupCreateRequest.equipamentId1()));
        equipamentGroup.setEquipament1(equipament1);

        if(equipamentGroupCreateRequest.equipamentId2() != null) {
            Equipament equipament2 = equipamentRepository.findById(equipamentGroupCreateRequest.equipamentId2())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.NOT_FOUND_MESSAGE + equipamentGroupCreateRequest.equipamentId2()));
            equipamentGroup.setEquipament2(equipament2);
        }
        if(equipamentGroupCreateRequest.equipamentId3() != null){
            Equipament equipament3 = equipamentRepository.findById(equipamentGroupCreateRequest.equipamentId3())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageConstants.NOT_FOUND_MESSAGE + equipamentGroupCreateRequest.equipamentId3()));
            equipamentGroup.setEquipament3(equipament3);
        }
        equipamentGroupRepository.save(equipamentGroup);
        return equipamentGroupMapper.toResponse(equipamentGroup);
    }
}
