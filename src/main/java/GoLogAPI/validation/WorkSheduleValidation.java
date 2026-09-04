package GoLogAPI.validation;

import GoLogAPI.model.enums.WorkScheduleStatus;
import GoLogAPI.repository.WorkScheduleRepository;
import org.springframework.stereotype.Component;

@Component
public class WorkSheduleValidation {

    private final WorkScheduleRepository workScheduleRepository;

    public WorkSheduleValidation(WorkScheduleRepository workScheduleRepository){
        this.workScheduleRepository = workScheduleRepository;
    }
}
