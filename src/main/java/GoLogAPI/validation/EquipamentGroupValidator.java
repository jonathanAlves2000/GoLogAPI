package GoLogAPI.validation;

import GoLogAPI.dto.equipamentGroup.EquipamentGroupCreateRequest;
import GoLogAPI.repository.EquipamentGroupRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class EquipamentGroupValidator {

    private EquipamentGroupRepository equipamentGroupRepository;

    public EquipamentGroupValidator(EquipamentGroupRepository equipamentGroupRepository){
        this.equipamentGroupRepository = equipamentGroupRepository;
    }

    ArrayList<String> errors = new ArrayList<>();

    public void validate(EquipamentGroupCreateRequest equipamentGroupCreateRequest){
        tractorValidate(equipamentGroupCreateRequest.equipament1Id(), errors);
        isEquipamentUsed(equipamentGroupCreateRequest.equipament2Id(), errors);
        isEquipamentUsed(equipamentGroupCreateRequest.equipament3Id(), errors);
    }

    public void tractorValidate(UUID equipamentId, List<String> errors){
        boolean isTractor = equipamentGroupRepository.isTractor(equipamentId);
        if(!isTractor) errors.add("O primeiro equipamento deve ser obrigatoriamente uma tração!");
    }

    private void isEquipamentUsed(UUID id, List<String> erros) {
        if(id == null) return;
        boolean isUsed = equipamentGroupRepository.isEquipamentUsed(id);
        if(isUsed) errors.add("Equipamento já está em uso por outro grupo ativo!");
    }
}
