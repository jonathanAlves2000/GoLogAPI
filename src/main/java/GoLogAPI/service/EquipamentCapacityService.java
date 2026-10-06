package GoLogAPI.service;

import GoLogAPI.dto.equipamentCapacity.EquipamentCapacityRequest;
import GoLogAPI.dto.equipamentCapacity.EquipamentCapacityResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.DemandType;
import GoLogAPI.model.Equipament;
import GoLogAPI.model.EquipamentCapacity;
import GoLogAPI.repository.DemandTypeRepository;
import GoLogAPI.repository.EquipamentCapacityRepository;
import GoLogAPI.repository.EquipamentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EquipamentCapacityService {

    private final EquipamentCapacityRepository equipamentCapacityRepository;
    private final EquipamentRepository equipamentRepository;
    private final DemandTypeRepository demandTypeRepository;

    public EquipamentCapacityService(EquipamentCapacityRepository equipamentCapacityRepository,
                                     EquipamentRepository equipamentRepository,
                                     DemandTypeRepository demandTypeRepository) {
        this.equipamentCapacityRepository = equipamentCapacityRepository;
        this.equipamentRepository = equipamentRepository;
        this.demandTypeRepository = demandTypeRepository;
    }

    @Transactional
    public List<EquipamentCapacityResponse> saveCapacities(UUID equipamentId, List<EquipamentCapacityRequest> requests) {
        Equipament equipament = equipamentRepository.findById(equipamentId)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, equipamentId));

        equipamentCapacityRepository.deleteByEquipamentId(equipamentId);

        List<EquipamentCapacity> capacitiesToSave = new ArrayList<>();
        for (EquipamentCapacityRequest req : requests) {
            DemandType demandType = demandTypeRepository.findById(req.demandTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Tipo de demanda não encontrado: ", req.demandTypeId()));

            EquipamentCapacity capacity = EquipamentCapacity.builder()
                    .equipament(equipament)
                    .demandType(demandType)
                    .maxCapacity(req.maxCapacity())
                    .softMaxCapacity(req.softMaxCapacity())
                    .costPerUnitAboveSoftMax(req.costPerUnitAboveSoftMax())
                    .build();
            capacitiesToSave.add(capacity);
        }

        List<EquipamentCapacity> saved = equipamentCapacityRepository.saveAll(capacitiesToSave);
        return saved.stream().map(this::toResponse).toList();
    }

    public List<EquipamentCapacityResponse> getCapacitiesByEquipamentId(UUID equipamentId) {
        return equipamentCapacityRepository.findByEquipamentId(equipamentId).stream()
                .map(this::toResponse)
                .toList();
    }

    private EquipamentCapacityResponse toResponse(EquipamentCapacity entity) {
        return new EquipamentCapacityResponse(
                entity.getId(),
                entity.getDemandType().getId(),
                entity.getDemandType().getCode(),
                entity.getDemandType().getName(),
                entity.getDemandType().getUnit(),
                entity.getMaxCapacity(),
                entity.getSoftMaxCapacity(),
                entity.getCostPerUnitAboveSoftMax()
        );
    }
}
