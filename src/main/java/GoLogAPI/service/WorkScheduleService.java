package GoLogAPI.service;

import GoLogAPI.dto.workSchedule.WorkSheduleCreateRequest;
import GoLogAPI.dto.workSchedule.WorkSheduleResponses;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantContext;
import GoLogAPI.model.Company;
import GoLogAPI.model.Driver;
import GoLogAPI.model.EquipamentGroup;
import GoLogAPI.model.WorkSchedule;
import GoLogAPI.model.WorkShiftTemplate;
import GoLogAPI.model.enums.WorkScheduleStatus;
import GoLogAPI.repository.DriverRepository;
import GoLogAPI.repository.EquipamentGroupRepository;
import GoLogAPI.repository.WorkScheduleRepository;
import GoLogAPI.repository.WorkShiftTemplateRepository;
import GoLogAPI.validation.WorkSheduleValidate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WorkScheduleService {

    private final WorkScheduleRepository workScheduleRepository;
    private final EquipamentGroupRepository equipamentGroupRepository;
    private final DriverRepository driverRepository;
    private final WorkShiftTemplateRepository workShiftTemplateRepository;
    private final WorkSheduleValidate workSheduleValidate;

    public WorkScheduleService(WorkScheduleRepository workScheduleRepository,
                               EquipamentGroupRepository equipamentGroupRepository,
                               DriverRepository driverRepository,
                               WorkShiftTemplateRepository workShiftTemplateRepository,
                               WorkSheduleValidate workSheduleValidate)
    {
        this.workScheduleRepository = workScheduleRepository;
        this.equipamentGroupRepository = equipamentGroupRepository;
        this.driverRepository = driverRepository;
        this.workShiftTemplateRepository = workShiftTemplateRepository;
        this.workSheduleValidate = workSheduleValidate;
    }

    @Transactional
    public void save(WorkSheduleCreateRequest request) {
        workSheduleValidate.validate(request);

        EquipamentGroup equipamentGroup = equipamentGroupRepository.findById(request.equipamentGroupId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, request.equipamentGroupId()));

        Driver driver = driverRepository.findById(request.driverId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, request.driverId()));

        Company company = equipamentGroup.getCompany() != null
                ? equipamentGroup.getCompany()
                : (driver.getUser() != null ? driver.getUser().getCompany() : null);

        WorkShiftTemplate template = null;
        if (request.shiftTemplateId() != null) {
            template = workShiftTemplateRepository.findById(request.shiftTemplateId()).orElse(null);
        }

        Integer breakDuration = request.breakDurationMinutes() != null
                ? request.breakDurationMinutes()
                : (template != null ? template.getBreakDurationMinutes() : 60);

        LocalTime earliestBreak = request.earliestBreakTime() != null
                ? request.earliestBreakTime()
                : (template != null ? template.getEarliestBreakTime() : null);

        LocalTime latestBreak = request.latestBreakTime() != null
                ? request.latestBreakTime()
                : (template != null ? template.getLatestBreakTime() : null);

        Integer maxDrivingHours = request.maxDrivingHoursWithoutBreak() != null
                ? request.maxDrivingHoursWithoutBreak()
                : (template != null ? template.getMaxDrivingHoursWithoutBreak() : 4);

        Integer minDrivingBreak = request.minDrivingBreakMinutes() != null
                ? request.minDrivingBreakMinutes()
                : (template != null ? template.getMinDrivingBreakMinutes() : 30);

        WorkSchedule workSchedule = WorkSchedule.builder()
                .driver(driver)
                .equipamentGroup(equipamentGroup)
                .company(company)
                .shiftTemplate(template)
                .scheduleDate(request.scheduleDate())
                .startWorkday(request.startWorkday())
                .endWorkday(request.endWorkday())
                .breakDurationMinutes(breakDuration)
                .earliestBreakTime(earliestBreak)
                .latestBreakTime(latestBreak)
                .maxDrivingHoursWithoutBreak(maxDrivingHours)
                .minDrivingBreakMinutes(minDrivingBreak)
                .status(request.status())
                .build();

        workScheduleRepository.save(workSchedule);
    }

    public List<WorkSheduleResponses> getAll() {
        UUID currentTenant = TenantContext.getCurrentTenantId();
        List<WorkSchedule> workSchedules = (currentTenant != null)
                ? workScheduleRepository.findByCompanyId(currentTenant)
                : workScheduleRepository.findAll();

        return workSchedules.stream().map(this::toResponse).toList();
    }

    public List<WorkSheduleResponses> getByStatus(WorkScheduleStatus status) {
        List<WorkSchedule> workSchedules = workScheduleRepository.findByStatus(status);
        return workSchedules.stream().map(this::toResponse).toList();
    }

    @Transactional
    public void update(UUID id, WorkSheduleCreateRequest request) {
        WorkSchedule workSchedule = workScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        EquipamentGroup equipamentGroup = equipamentGroupRepository.findById(request.equipamentGroupId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, request.equipamentGroupId()));

        Driver driver = driverRepository.findById(request.driverId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, request.driverId()));

        WorkShiftTemplate template = null;
        if (request.shiftTemplateId() != null) {
            template = workShiftTemplateRepository.findById(request.shiftTemplateId()).orElse(null);
        }

        workSchedule.setDriver(driver);
        workSchedule.setEquipamentGroup(equipamentGroup);
        workSchedule.setShiftTemplate(template);
        workSchedule.setScheduleDate(request.scheduleDate());
        workSchedule.setStartWorkday(request.startWorkday());
        workSchedule.setEndWorkday(request.endWorkday());
        workSchedule.setStatus(request.status());

        if (request.breakDurationMinutes() != null) {
            workSchedule.setBreakDurationMinutes(request.breakDurationMinutes());
        } else if (template != null) {
            workSchedule.setBreakDurationMinutes(template.getBreakDurationMinutes());
        }

        if (request.earliestBreakTime() != null) {
            workSchedule.setEarliestBreakTime(request.earliestBreakTime());
        } else if (template != null) {
            workSchedule.setEarliestBreakTime(template.getEarliestBreakTime());
        }

        if (request.latestBreakTime() != null) {
            workSchedule.setLatestBreakTime(request.latestBreakTime());
        } else if (template != null) {
            workSchedule.setLatestBreakTime(template.getLatestBreakTime());
        }

        if (request.maxDrivingHoursWithoutBreak() != null) {
            workSchedule.setMaxDrivingHoursWithoutBreak(request.maxDrivingHoursWithoutBreak());
        } else if (template != null) {
            workSchedule.setMaxDrivingHoursWithoutBreak(template.getMaxDrivingHoursWithoutBreak());
        }

        if (request.minDrivingBreakMinutes() != null) {
            workSchedule.setMinDrivingBreakMinutes(request.minDrivingBreakMinutes());
        } else if (template != null) {
            workSchedule.setMinDrivingBreakMinutes(template.getMinDrivingBreakMinutes());
        }

        workScheduleRepository.save(workSchedule);
    }

    @Transactional
    public void delete(UUID id) {
        WorkSchedule workSchedule = workScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        workScheduleRepository.delete(workSchedule);
    }

    private WorkSheduleResponses toResponse(WorkSchedule workSchedule) {
        return new WorkSheduleResponses(
                workSchedule.getId(),
                workSchedule.getDriver(),
                workSchedule.getEquipamentGroup(),
                workSchedule.getScheduleDate(),
                workSchedule.getStartWorkday(),
                workSchedule.getEndWorkday(),
                workSchedule.getDriver().getCostPerHour(),
                workSchedule.getStatus(),
                workSchedule.getShiftTemplate(),
                workSchedule.getBreakDurationMinutes(),
                workSchedule.getEarliestBreakTime(),
                workSchedule.getLatestBreakTime(),
                workSchedule.getMaxDrivingHoursWithoutBreak(),
                workSchedule.getMinDrivingBreakMinutes()
        );
    }
}
