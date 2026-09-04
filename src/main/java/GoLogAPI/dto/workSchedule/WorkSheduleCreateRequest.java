package GoLogAPI.dto.workSchedule;

import GoLogAPI.model.enums.WorkScheduleStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record WorkSheduleCreateRequest(

        @NotNull(message = "O Id do motorista deve ser informado.")
        UUID driverId,
        @NotNull(message = "O Id do conjunto de equipamento deve ser infromado.")
        UUID equipamentGroupId,
        @NotNull(message = "A data da escala deve ser informada.")
        LocalDate scheduleDate,
        @NotNull(message = "O Status da escala deve ser informado")
        WorkScheduleStatus status
) { }
