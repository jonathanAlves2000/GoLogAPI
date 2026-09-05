package GoLogAPI.validation;

import GoLogAPI.dto.workSchedule.WorkScheduleRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.WorkScheduleRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class WorkSheduleValidation {

    private final WorkScheduleRepository workScheduleRepository;

    public WorkSheduleValidation(WorkScheduleRepository workScheduleRepository){
        this.workScheduleRepository = workScheduleRepository;
    }

    public void validate(WorkScheduleRequest workScheduleRequest){
        List<String> errors = new ArrayList<>();

        driver(workScheduleRequest.driverId(), errors);
        equipamentGroup(workScheduleRequest.equipamentGroupId(), errors);

        if(!errors.isEmpty())
            throw new ConflictException(errors);
    }

    public void driver(UUID driverId, List<String> errors){
        boolean exists = workScheduleRepository.existsByDriverId(driverId);

        if(exists)
            errors.add("Já existe uma escala ativa para esse motorista.");
    }

    public void equipamentGroup(UUID equipamentGroupId, List<String> errors){
        boolean exists = workScheduleRepository.existsByEquipamentGroupId(equipamentGroupId);

        if(exists)
            errors.add("Conjunto de equipamento já está sendo utilizado por um motorista");
    }

}
