package GoLogAPI.service;

import GoLogAPI.dto.supportedType.SupportedTypeCreateRequest;
import GoLogAPI.dto.supportedType.SupportedTypeResponse;
import GoLogAPI.dto.equipamentGroup.EquipamentGroupCreateRequest;
import GoLogAPI.dto.equipamentGroup.EquipamentGroupUpdateRequest;
import GoLogAPI.dto.equipamentGroup.EquipamentGroupResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.EquipamentGroupMapper;
import GoLogAPI.mapper.SupportedTypeMapper;
import GoLogAPI.model.Equipament;
import GoLogAPI.model.EquipamentGroup;
import GoLogAPI.model.TypeTransport;
import GoLogAPI.repository.EquipamentGroupRepository;
import GoLogAPI.repository.EquipamentRepository;
import GoLogAPI.repository.TypeTransportRepository;
import GoLogAPI.validation.EquipamentGroupValidator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EquipamentGroupService {

    private final EquipamentGroupRepository equipamentGroupRepository;
    private final EquipamentGroupMapper equipamentGroupMapper;
    private final EquipamentRepository equipamentRepository;
    private final EquipamentGroupValidator equipamentGroupValidator;
    private final TypeTransportRepository typeTransportRepository;
    private final SupportedTypeMapper supportedTypeMapper;


    public EquipamentGroupService(EquipamentGroupRepository equipamentGroupRepository,
    EquipamentGroupMapper equipamentGroupMapper,EquipamentRepository equipamentRepository,
    EquipamentGroupValidator equipamentGroupValidator, TypeTransportRepository typeTransportRepository,
                                  SupportedTypeMapper supportedTypeMapper){
        this.equipamentGroupRepository = equipamentGroupRepository;
        this.equipamentGroupMapper = equipamentGroupMapper;
        this.equipamentRepository = equipamentRepository;
        this.equipamentGroupValidator= equipamentGroupValidator;
        this.typeTransportRepository = typeTransportRepository;
        this.supportedTypeMapper = supportedTypeMapper;
    }

    @Transactional
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

    public List<EquipamentGroupResponse> getAll() {
        List<EquipamentGroup> equipamentGroups = equipamentGroupRepository.findAll();
        return equipamentGroupMapper.toResponses(equipamentGroups);
    }

    @Transactional
    public void delete(UUID id){
        EquipamentGroup equipamentGroup = equipamentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        equipamentGroupRepository.delete(equipamentGroup);
    }

    @Transactional
    public EquipamentGroupResponse update(UUID id, EquipamentGroupCreateRequest equipamentGroupCreateRequest){
        equipamentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        EquipamentGroup equipamentGroup = equipamentGroupMapper.toEntity(equipamentGroupCreateRequest);

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

        equipamentGroup.setId(id);
        equipamentGroupRepository.save(equipamentGroup);
        return equipamentGroupMapper.toResponse(equipamentGroup);
    }

    @Transactional
    public EquipamentGroupResponse updatePartial(UUID id, EquipamentGroupUpdateRequest equipamentGroupUpdateRequest){
        EquipamentGroup equipamentGroup = equipamentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        if(equipamentGroupUpdateRequest.observation() != null)
            equipamentGroup.setObservation(equipamentGroupUpdateRequest.observation());
        if(equipamentGroupUpdateRequest.equipament1Id() != null){
            Equipament equipament1 = equipamentRepository.findById(equipamentGroupUpdateRequest.equipament1Id())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipamentGroupUpdateRequest.equipament1Id()));
            equipamentGroup.setEquipament1(equipament1);
        }
        if(equipamentGroupUpdateRequest.equipament2Id() != null){
            Equipament equipament2 = equipamentRepository.findById(equipamentGroupUpdateRequest.equipament2Id())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipamentGroupUpdateRequest.equipament2Id()));
            equipamentGroup.setEquipament2(equipament2);
        }
        if(equipamentGroupUpdateRequest.equipament3Id() != null){
            Equipament equipament3 = equipamentRepository.findById(equipamentGroupUpdateRequest.equipament3Id())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipamentGroupUpdateRequest.equipament3Id()));
            equipamentGroup.setEquipament3(equipament3);
        }

        equipamentGroupRepository.save(equipamentGroup);
        return equipamentGroupMapper.toResponse(equipamentGroup);
    }

    @Transactional
    public SupportedTypeResponse addTransportType(SupportedTypeCreateRequest supportedTypeCreateRequest){
        EquipamentGroup equipamentGroup = equipamentGroupRepository.findById(supportedTypeCreateRequest.groupId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, supportedTypeCreateRequest.groupId()));

        TypeTransport typeTransport = typeTransportRepository.findById(supportedTypeCreateRequest.typeId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, supportedTypeCreateRequest.typeId()));

        equipamentGroup.getTypeTransports().add(typeTransport);

        equipamentGroupRepository.save(equipamentGroup);

        return supportedTypeMapper.toResponse(equipamentGroup, typeTransport);
    }
}
