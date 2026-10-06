package GoLogAPI.dto.workShiftTemplate;

import GoLogAPI.model.enums.WorkShiftType;

import java.time.LocalTime;
import java.util.UUID;

public record WorkShiftTemplateResponse(
        UUID id,
        String name,
        String description,
        WorkShiftType shiftType,
        LocalTime defaultStartWorkday,
        LocalTime defaultEndWorkday,
        Integer breakDurationMinutes,
        LocalTime earliestBreakTime,
        LocalTime latestBreakTime,
        Integer maxDrivingHoursWithoutBreak,
        Integer minDrivingBreakMinutes,
        Boolean isDefault,
        Boolean isSystemGlobal
) { }
