package GoLogAPI.dto.workShiftTemplate;

import GoLogAPI.model.enums.WorkShiftType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record WorkShiftTemplateCreateRequest(
        @NotBlank(message = "O nome do modelo de turno deve ser informado.")
        String name,

        String description,

        @NotNull(message = "O tipo do turno deve ser informado.")
        WorkShiftType shiftType,

        @NotNull(message = "O horário de início padrão deve ser informado.")
        LocalTime defaultStartWorkday,

        @NotNull(message = "O horário de término padrão deve ser informado.")
        LocalTime defaultEndWorkday,

        Integer breakDurationMinutes,

        LocalTime earliestBreakTime,

        LocalTime latestBreakTime,

        Integer maxDrivingHoursWithoutBreak,

        Integer minDrivingBreakMinutes,

        Boolean isDefault
) { }
