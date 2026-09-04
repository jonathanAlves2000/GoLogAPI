package GoLogAPI.service;

import GoLogAPI.dto.workSchedule.WorkSheduleCreateRequest;
import GoLogAPI.dto.workSchedule.WorkSheduleResponses;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.Driver;
import GoLogAPI.model.EquipamentGroup;
import GoLogAPI.model.WorkSchedule;
import GoLogAPI.repository.DriverRepository;
import GoLogAPI.repository.EquipamentGroupRepository;
import GoLogAPI.repository.WorkScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final EquipamentGroupRepository equipamentGroupRepository;
    private final DriverRepository driverRepository;

    public WorkScheduleService(WorkScheduleRepository workScheduleRepository,
                               EquipamentGroupRepository equipamentGroupRepository,
                               DriverRepository driverRepository)
    {
        this.workScheduleRepository = workScheduleRepository;
        this.equipamentGroupRepository = equipamentGroupRepository;
        this.driverRepository = driverRepository;
    }

    @Transactional
    public void save(WorkSheduleCreateRequest workSheduleCreateRequest){

        EquipamentGroup equipamentGroup = equipamentGroupRepository.findById(workSheduleCreateRequest.equipamentGroupId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, workSheduleCreateRequest.equipamentGroupId()));

        Driver driver = driverRepository.findById(workSheduleCreateRequest.driverId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, workSheduleCreateRequest.driverId()));

        WorkSchedule workSchedule = WorkSchedule.builder()
                .driver(driver)
                .equipamentGroup(equipamentGroup)
                .scheduleDate(workSheduleCreateRequest.scheduleDate())
                .status(workSheduleCreateRequest.status())
                .build();

        workScheduleRepository.save(workSchedule);
    }

    public List<WorkSheduleResponses> getAll(){
        List<WorkSchedule> workSchedules = workScheduleRepository.findAll();

        return workSchedules.stream()
                .map(workSchedule -> new WorkSheduleResponses(
                        workSchedule.getId(),
                        workSchedule.getDriver(),
                        workSchedule.getEquipamentGroup(),
                        workSchedule.getScheduleDate(),
                        workSchedule.getStatus()
                )).toList();
    }

    @Transactional
    public void update(UUID id, WorkSheduleCreateRequest workSheduleCreateRequest) {

        WorkSchedule workSchedule = workScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        EquipamentGroup equipamentGroup = equipamentGroupRepository.findById(workSheduleCreateRequest.equipamentGroupId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, workSheduleCreateRequest.equipamentGroupId()));

        Driver driver = driverRepository.findById(workSheduleCreateRequest.driverId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, workSheduleCreateRequest.driverId()));

        workSchedule.setDriver(driver);
        workSchedule.setEquipamentGroup(equipamentGroup);
        workSchedule.setScheduleDate(workSheduleCreateRequest.scheduleDate());
        workSchedule.setStatus(workSheduleCreateRequest.status());

        workScheduleRepository.save(workSchedule);
    }

    @Transactional
    public void delete(UUID id){
        WorkSchedule workSchedule = workScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        workScheduleRepository.delete(workSchedule);
    }

}
