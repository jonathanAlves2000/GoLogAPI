package GoLogAPI.validation;

import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.EquipamentGroupRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Regras de composição de um conjunto: o 1º equipamento é obrigatoriamente uma tração, os demais são
 * reboques, não há repetição e nenhum equipamento pode estar em outro conjunto ativo.
 */
@Component
public class EquipamentGroupValidate {

    private final EquipamentGroupRepository equipamentGroupRepository;

    public EquipamentGroupValidate(EquipamentGroupRepository equipamentGroupRepository){
        this.equipamentGroupRepository = equipamentGroupRepository;
    }

    /**
     * @param selfGroupId id do conjunto em edição (seus próprios equipamentos não contam como "em uso")
     */
    public void validate(UUID equipament1Id, UUID equipament2Id, UUID equipament3Id, UUID selfGroupId){
        List<String> errors = new ArrayList<>();

        if (equipament1Id != null && !equipamentGroupRepository.isTractor(equipament1Id))
            errors.add("O primeiro equipamento deve ser obrigatoriamente uma tração (cavalo/caminhão)!");
        if (equipament2Id != null && equipamentGroupRepository.isTractor(equipament2Id))
            errors.add("O 2º equipamento deve ser uma carreta/reboque, não uma tração!");
        if (equipament3Id != null && equipamentGroupRepository.isTractor(equipament3Id))
            errors.add("O 3º equipamento deve ser uma carreta/reboque, não uma tração!");
        if (equipament2Id != null && equipament2Id.equals(equipament3Id))
            errors.add("A mesma carreta não pode ser usada duas vezes no conjunto!");

        for (UUID id : new UUID[]{equipament1Id, equipament2Id, equipament3Id}) {
            if (id != null && isUsedByAnotherGroup(id, selfGroupId)) {
                errors.add("Um dos equipamentos selecionados já está em uso por outro conjunto ativo!");
                break;
            }
        }

        if(!errors.isEmpty()) throw new ConflictException(errors);
    }

    private boolean isUsedByAnotherGroup(UUID equipamentId, UUID selfGroupId) {
        return equipamentGroupRepository.findAll().stream()
                .filter(g -> !Objects.equals(g.getId(), selfGroupId))
                .anyMatch(g -> (g.getEquipament1() != null && equipamentId.equals(g.getEquipament1().getId()))
                        || (g.getEquipament2() != null && equipamentId.equals(g.getEquipament2().getId()))
                        || (g.getEquipament3() != null && equipamentId.equals(g.getEquipament3().getId())));
    }
}
