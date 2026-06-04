package GoLogAPI.validation;

import GoLogAPI.dto.equipamentGroup.EquipamentGroupCreateRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.EquipamentGroupRepository;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class EquipamentGroupValidator {

    private final EquipamentGroupRepository equipamentGroupRepository;

    public EquipamentGroupValidator(EquipamentGroupRepository equipamentGroupRepository){
        this.equipamentGroupRepository = equipamentGroupRepository;
    }

    public void validate(EquipamentGroupCreateRequest equipamentGroupCreateRequest){
        ArrayList<String> errors = new ArrayList<>();
        if(equipamentGroupCreateRequest.equipament1Id() != null)
            isTractor(equipamentGroupCreateRequest.equipament1Id(), errors);

        if(equipamentGroupCreateRequest.equipament1Id() != null)
            isEquipamentUsed(equipamentGroupCreateRequest.equipament1Id(), errors);

        if(equipamentGroupCreateRequest.equipament2Id() != null)
            isEquipamentUsed(equipamentGroupCreateRequest.equipament2Id(), errors);

        if(equipamentGroupCreateRequest.equipament3Id() != null)
            isEquipamentUsed(equipamentGroupCreateRequest.equipament3Id(), errors);

        if(equipamentGroupCreateRequest.equipament2Id() != null)
            isNotTractor(equipamentGroupCreateRequest.equipament2Id(), errors);

        if(equipamentGroupCreateRequest.equipament3Id() != null)
            isNotTractor(equipamentGroupCreateRequest.equipament3Id(), errors);

        if(!errors.isEmpty()) throw new ConflictException(errors);
    }

    public void isTractor(UUID equipamentId, List<String> error){
        boolean isTractor = equipamentGroupRepository.isTractor(equipamentId);
        if(!isTractor) error.add("O primeiro equipamento deve ser obrigatoriamente uma tração!");
    }

    private void isEquipamentUsed(UUID id, List<String> error) {
        if(id == null) return;
        boolean isUsed = equipamentGroupRepository.isEquipamentUsed(id);
        if(isUsed) error.add("Equipamento já está em uso por outro grupo ativo!");
    }

    public void isNotTractor(UUID id, List<String> error){
        if(id == null) return;
        boolean isTractor = equipamentGroupRepository.isTractor(id);
        if(isTractor) error.add("Equipamentos secundarios não podem ser tração!");
    }
}
