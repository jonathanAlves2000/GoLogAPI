package GoLogAPI.service;

import GoLogAPI.dto.workShiftTemplate.WorkShiftTemplateCreateRequest;
import GoLogAPI.dto.workShiftTemplate.WorkShiftTemplateResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantContext;
import GoLogAPI.model.Company;
import GoLogAPI.model.WorkShiftTemplate;
import GoLogAPI.model.enums.WorkShiftType;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.repository.WorkShiftTemplateRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class WorkShiftTemplateService {

    private final WorkShiftTemplateRepository workShiftTemplateRepository;
    private final CompanyRepository companyRepository;

    public WorkShiftTemplateService(WorkShiftTemplateRepository workShiftTemplateRepository,
                                    CompanyRepository companyRepository) {
        this.workShiftTemplateRepository = workShiftTemplateRepository;
        this.companyRepository = companyRepository;
    }

    @PostConstruct
    @Transactional
    public void seedDefaultGlobalTemplates() {
        if (workShiftTemplateRepository.findByCompanyIdIsNull().isEmpty()) {
            List<WorkShiftTemplate> defaults = List.of(
                    WorkShiftTemplate.builder()
                            .name("Comercial Padrão (08h às 18h)")
                            .description("Jornada comercial padrão de 44h semanais com 1h de almoço e intervalo a cada 4h de direção.")
                            .shiftType(WorkShiftType.COMERCIAL_PADRAO)
                            .defaultStartWorkday(LocalTime.of(8, 0))
                            .defaultEndWorkday(LocalTime.of(18, 0))
                            .breakDurationMinutes(60)
                            .earliestBreakTime(LocalTime.of(11, 30))
                            .latestBreakTime(LocalTime.of(13, 30))
                            .maxDrivingHoursWithoutBreak(4)
                            .minDrivingBreakMinutes(30)
                            .isDefault(true)
                            .company(null)
                            .build(),

                    WorkShiftTemplate.builder()
                            .name("Escala 12x36 Diurno (07h às 19h)")
                            .description("Turno de revezamento diurno com 1h de refeição e pausas obrigatórias da Lei 13.103.")
                            .shiftType(WorkShiftType.ESCALA_12X36_DIURNO)
                            .defaultStartWorkday(LocalTime.of(7, 0))
                            .defaultEndWorkday(LocalTime.of(19, 0))
                            .breakDurationMinutes(60)
                            .earliestBreakTime(LocalTime.of(11, 30))
                            .latestBreakTime(LocalTime.of(14, 0))
                            .maxDrivingHoursWithoutBreak(4)
                            .minDrivingBreakMinutes(30)
                            .isDefault(false)
                            .company(null)
                            .build(),

                    WorkShiftTemplate.builder()
                            .name("Escala 12x36 Noturno (19h às 07h)")
                            .description("Turno de revezamento noturno com 1h de intervalo para refeição noturna.")
                            .shiftType(WorkShiftType.ESCALA_12X36_NOTURNO)
                            .defaultStartWorkday(LocalTime.of(19, 0))
                            .defaultEndWorkday(LocalTime.of(7, 0))
                            .breakDurationMinutes(60)
                            .earliestBreakTime(LocalTime.of(23, 30))
                            .latestBreakTime(LocalTime.of(2, 0))
                            .maxDrivingHoursWithoutBreak(4)
                            .minDrivingBreakMinutes(30)
                            .isDefault(false)
                            .company(null)
                            .build(),

                    WorkShiftTemplate.builder()
                            .name("Turno Noturno Fechado (22h às 06h)")
                            .description("Turno noturno fechado para operações de transferência e entregas expressas de madrugada.")
                            .shiftType(WorkShiftType.TURNO_NOTURNO)
                            .defaultStartWorkday(LocalTime.of(22, 0))
                            .defaultEndWorkday(LocalTime.of(6, 0))
                            .breakDurationMinutes(60)
                            .earliestBreakTime(LocalTime.of(1, 0))
                            .latestBreakTime(LocalTime.of(3, 0))
                            .maxDrivingHoursWithoutBreak(4)
                            .minDrivingBreakMinutes(30)
                            .isDefault(false)
                            .company(null)
                            .build(),

                    WorkShiftTemplate.builder()
                            .name("Diarista Flexível (08h às 17h)")
                            .description("Jornada flexível diária para frotistas terceirizados e autônomos.")
                            .shiftType(WorkShiftType.DIARISTA_FLEXIVEL)
                            .defaultStartWorkday(LocalTime.of(8, 0))
                            .defaultEndWorkday(LocalTime.of(17, 0))
                            .breakDurationMinutes(60)
                            .earliestBreakTime(LocalTime.of(11, 30))
                            .latestBreakTime(LocalTime.of(13, 30))
                            .maxDrivingHoursWithoutBreak(4)
                            .minDrivingBreakMinutes(30)
                            .isDefault(false)
                            .company(null)
                            .build()
            );

            workShiftTemplateRepository.saveAll(defaults);
        }
    }

    public List<WorkShiftTemplateResponse> getAll() {
        UUID currentTenant = TenantContext.getCurrentTenantId();
        List<WorkShiftTemplate> templates = new ArrayList<>();

        // Adiciona templates específicos da empresa
        if (currentTenant != null) {
            templates.addAll(workShiftTemplateRepository.findByCompanyId(currentTenant));
        }

        // Adiciona os modelos pré-definidos globais do sistema
        templates.addAll(workShiftTemplateRepository.findByCompanyIdIsNull());

        return templates.stream().map(this::toResponse).toList();
    }

    public WorkShiftTemplateResponse getById(UUID id) {
        WorkShiftTemplate template = workShiftTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return toResponse(template);
    }

    public WorkShiftTemplate getEntityById(UUID id) {
        return workShiftTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
    }

    @Transactional
    public WorkShiftTemplateResponse save(WorkShiftTemplateCreateRequest request) {
        UUID currentTenant = TenantContext.getCurrentTenantId();
        Company company = (currentTenant != null)
                ? companyRepository.findById(currentTenant).orElse(null)
                : null;

        WorkShiftTemplate template = WorkShiftTemplate.builder()
                .name(request.name())
                .description(request.description())
                .shiftType(request.shiftType())
                .defaultStartWorkday(request.defaultStartWorkday())
                .defaultEndWorkday(request.defaultEndWorkday())
                .breakDurationMinutes(request.breakDurationMinutes() != null ? request.breakDurationMinutes() : 60)
                .earliestBreakTime(request.earliestBreakTime())
                .latestBreakTime(request.latestBreakTime())
                .maxDrivingHoursWithoutBreak(request.maxDrivingHoursWithoutBreak() != null ? request.maxDrivingHoursWithoutBreak() : 4)
                .minDrivingBreakMinutes(request.minDrivingBreakMinutes() != null ? request.minDrivingBreakMinutes() : 30)
                .isDefault(Boolean.TRUE.equals(request.isDefault()))
                .company(company)
                .build();

        return toResponse(workShiftTemplateRepository.save(template));
    }

    @Transactional
    public WorkShiftTemplateResponse update(UUID id, WorkShiftTemplateCreateRequest request) {
        WorkShiftTemplate template = workShiftTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));

        template.setName(request.name());
        template.setDescription(request.description());
        template.setShiftType(request.shiftType());
        template.setDefaultStartWorkday(request.defaultStartWorkday());
        template.setDefaultEndWorkday(request.defaultEndWorkday());
        if (request.breakDurationMinutes() != null) template.setBreakDurationMinutes(request.breakDurationMinutes());
        template.setEarliestBreakTime(request.earliestBreakTime());
        template.setLatestBreakTime(request.latestBreakTime());
        if (request.maxDrivingHoursWithoutBreak() != null) template.setMaxDrivingHoursWithoutBreak(request.maxDrivingHoursWithoutBreak());
        if (request.minDrivingBreakMinutes() != null) template.setMinDrivingBreakMinutes(request.minDrivingBreakMinutes());
        if (request.isDefault() != null) template.setIsDefault(request.isDefault());

        return toResponse(workShiftTemplateRepository.save(template));
    }

    @Transactional
    public void delete(UUID id) {
        WorkShiftTemplate template = workShiftTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        workShiftTemplateRepository.delete(template);
    }

    private WorkShiftTemplateResponse toResponse(WorkShiftTemplate template) {
        return new WorkShiftTemplateResponse(
                template.getId(),
                template.getName(),
                template.getDescription(),
                template.getShiftType(),
                template.getDefaultStartWorkday(),
                template.getDefaultEndWorkday(),
                template.getBreakDurationMinutes(),
                template.getEarliestBreakTime(),
                template.getLatestBreakTime(),
                template.getMaxDrivingHoursWithoutBreak(),
                template.getMinDrivingBreakMinutes(),
                template.getIsDefault(),
                template.getCompany() == null
        );
    }
}
