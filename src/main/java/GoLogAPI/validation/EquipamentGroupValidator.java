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

    private EquipamentGroupRepository equipamentGroupRepository;

    public EquipamentGroupValidator(EquipamentGroupRepository equipamentGroupRepository){
        this.equipamentGroupRepository = equipamentGroupRepository;
    }

    ArrayList<String> errors = new ArrayList<>();

    public void validate(EquipamentGroupCreateRequest equipamentGroupCreateRequest){
        isTractor(equipamentGroupCreateRequest.equipament1Id());
        isEquipamentUsed(equipamentGroupCreateRequest.equipament1Id(), errors);
        isEquipamentUsed(equipamentGroupCreateRequest.equipament2Id(), errors);
        isEquipamentUsed(equipamentGroupCreateRequest.equipament3Id(), errors);
        isNotTractor(equipamentGroupCreateRequest.equipament2Id(), errors);
        isNotTractor(equipamentGroupCreateRequest.equipament3Id(), errors);

        if(!errors.isEmpty()) throw new ConflictException(errors);
    }

    public void isTractor(UUID equipamentId){
        boolean isTractor = equipamentGroupRepository.isTractor(equipamentId);
        if(!isTractor) errors.add("O primeiro equipamento deve ser obrigatoriamente uma tração!");
    }

    private void isEquipamentUsed(UUID id, List<String> erros) {
        if(id == null) return;
        boolean isUsed = equipamentGroupRepository.isEquipamentUsed(id);
        System.out.println("teste de validação" + isUsed);
        if(isUsed) errors.add("Equipamento já está em uso por outro grupo ativo!");
    }

    public void isNotTractor(UUID id, List<String> errors){
        if(id == null) return;
        boolean isTractor = equipamentGroupRepository.isNotTractor(id);
        if(isTractor) errors.add("Equipamentos secundarios não podem ser tração!");
    }
}
