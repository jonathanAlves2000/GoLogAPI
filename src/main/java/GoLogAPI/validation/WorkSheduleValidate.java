package GoLogAPI.validation;

import GoLogAPI.dto.workSchedule.WorkScheduleRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.model.WorkSchedule;
import GoLogAPI.model.enums.WorkScheduleStatus;
import GoLogAPI.repository.WorkScheduleRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Impede que o mesmo motorista ou o mesmo conjunto tenha duas escalas ativas com horários sobrepostos.
 *
 * <p>A regra antiga bloqueava qualquer segunda escala ativa do motorista, independentemente da data —
 * o que tornava impossível planejar mais de um dia da operação.</p>
 */
@Component
public class WorkSheduleValidate {

    private final WorkScheduleRepository workScheduleRepository;

    public WorkSheduleValidate(WorkScheduleRepository workScheduleRepository){
        this.workScheduleRepository = workScheduleRepository;
    }

    public void validate(WorkScheduleRequest request){
        validate(request, null);
    }

    public void validate(WorkScheduleRequest request, UUID selfId){
        List<String> errors = new ArrayList<>();
        if (request.status() == WorkScheduleStatus.DESATIVADO) return;

        if (request.startWorkday() != null && request.endWorkday() != null
                && request.startWorkday().equals(request.endWorkday())) {
            errors.add("O início e o fim do turno não podem ser iguais.");
        }

        // Turnos noturnos podem invadir o dia seguinte/anterior: consulta só a vizinhança da data
        LocalDate day = request.scheduleDate() != null ? request.scheduleDate() : LocalDate.now();
        List<WorkSchedule> active = workScheduleRepository
                .findForOverlapCheck(request.driverId(), request.equipamentGroupId(), day.minusDays(1), day.plusDays(1)).stream()
                .filter(w -> !Objects.equals(w.getId(), selfId))
                .filter(w -> w.getStatus() == WorkScheduleStatus.ATIVO || w.getStatus() == WorkScheduleStatus.EM_OPERACAO)
                .filter(w -> overlaps(w, request.scheduleDate(), request.startWorkday(), request.endWorkday()))
                .toList();

        if (active.stream().anyMatch(w -> w.getDriver() != null && w.getDriver().getId().equals(request.driverId())))
            errors.add("Este motorista já possui uma escala ativa que se sobrepõe a este horário.");
        if (active.stream().anyMatch(w -> w.getEquipamentGroup() != null && w.getEquipamentGroup().getId().equals(request.equipamentGroupId())))
            errors.add("Este conjunto de equipamentos já está escalado em um horário que se sobrepõe a este.");

        if(!errors.isEmpty())
            throw new ConflictException(errors);
    }

    private static boolean overlaps(WorkSchedule w, LocalDate date, LocalTime start, LocalTime end) {
        if (w.getScheduleDate() == null || date == null || w.getStartWorkday() == null || start == null) {
            return Objects.equals(w.getScheduleDate(), date);
        }
        LocalDateTime[] a = interval(w.getScheduleDate(), w.getStartWorkday(), w.getEndWorkday());
        LocalDateTime[] b = interval(date, start, end);
        return a[0].isBefore(b[1]) && b[0].isBefore(a[1]);
    }

    /** Turnos que viram a noite (fim &lt; início) terminam no dia seguinte. */
    private static LocalDateTime[] interval(LocalDate date, LocalTime start, LocalTime end) {
        LocalDateTime s = date.atTime(start);
        LocalDateTime e = date.atTime(end != null ? end : start.plusHours(8));
        if (!e.isAfter(s)) e = e.plusDays(1);
        return new LocalDateTime[]{s, e};
    }
}
