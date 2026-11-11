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
    private final GoLogAPI.repository.CompanyRepository companyRepository;
    private final GoLogAPI.infra.tenant.TenantGuard tenantGuard;

    public WorkScheduleService(WorkScheduleRepository workScheduleRepository,
                               EquipamentGroupRepository equipamentGroupRepository,
                               DriverRepository driverRepository,
                               WorkShiftTemplateRepository workShiftTemplateRepository,
                               WorkSheduleValidate workSheduleValidate,
                               GoLogAPI.repository.CompanyRepository companyRepository,
                               GoLogAPI.infra.tenant.TenantGuard tenantGuard)
    {
        this.tenantGuard = tenantGuard;
        this.workScheduleRepository = workScheduleRepository;
        this.equipamentGroupRepository = equipamentGroupRepository;
        this.driverRepository = driverRepository;
        this.workShiftTemplateRepository = workShiftTemplateRepository;
        this.workSheduleValidate = workSheduleValidate;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public WorkSheduleResponses save(WorkSheduleCreateRequest request) {
        workSheduleValidate.validate(request);

        EquipamentGroup equipamentGroup = findGroup(request.equipamentGroupId());
        Driver driver = findDriver(request.driverId());
        Company company = equipamentGroup.getCompany() != null ? equipamentGroup.getCompany() : tenantGuard.ownerForWrite(null);
        assertSameTenant(company, driver);

        WorkShiftTemplate template = findTemplate(request.shiftTemplateId());

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
        return toResponse(workSchedule);
    }

    public List<WorkSheduleResponses> getAll() {
        List<WorkSchedule> workSchedules = tenantGuard.isGlobalView()
                ? workScheduleRepository.findAll()
                : workScheduleRepository.findByCompanyId(tenantGuard.scope());

        return workSchedules.stream().map(this::toResponse).toList();
    }

    public List<WorkSheduleResponses> getByStatus(WorkScheduleStatus status) {
        return getAll().stream().filter(w -> w.status() == status).toList();
    }

    @Transactional
    public void update(UUID id, WorkSheduleCreateRequest request) {
        WorkSchedule workSchedule = findVisible(id);
        workSheduleValidate.validate(request, id);

        EquipamentGroup equipamentGroup = findGroup(request.equipamentGroupId());
        Driver driver = findDriver(request.driverId());
        assertSameTenant(equipamentGroup.getCompany(), driver);
        WorkShiftTemplate template = findTemplate(request.shiftTemplateId());
        if (equipamentGroup.getCompany() != null) workSchedule.setCompany(equipamentGroup.getCompany());

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
        WorkSchedule workSchedule = findVisible(id);

        workScheduleRepository.delete(workSchedule);
    }

    public WorkSchedule findVisible(UUID id) {
        WorkSchedule ws = workScheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Escala não encontrada: ", id));
        tenantGuard.checkAccess(ws.getCompany(), id);
        return ws;
    }

    private EquipamentGroup findGroup(UUID id) {
        EquipamentGroup group = equipamentGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conjunto não encontrado: ", id));
        tenantGuard.checkAccess(group.getCompany(), id);
        return group;
    }

    private Driver findDriver(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Motorista não encontrado: ", id));
        tenantGuard.checkAccess(driver.getCompany(), id);
        return driver;
    }

    private WorkShiftTemplate findTemplate(UUID id) {
        if (id == null) return null;
        WorkShiftTemplate template = workShiftTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Modelo de jornada não encontrado: ", id));
        tenantGuard.checkCatalogRead(template.getCompany(), id);
        return template;
    }

    private static void assertSameTenant(Company company, Driver driver) {
        if (company != null && driver.getCompany() != null && !company.getId().equals(driver.getCompany().getId())) {
            throw new GoLogAPI.exception.BusinessException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Motorista e conjunto pertencem a empresas diferentes. Escale um motorista do mesmo tenant do conjunto.");
        }
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
