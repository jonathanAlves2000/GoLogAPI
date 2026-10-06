package GoLogAPI.dto.workSchedule;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import GoLogAPI.model.enums.WorkScheduleStatus;

public interface WorkScheduleRequest {

    UUID driverId();
    UUID equipamentGroupId();
    LocalDate scheduleDate();
    WorkScheduleStatus status();
    LocalTime startWorkday();
    LocalTime endWorkday();

    UUID shiftTemplateId();
    Integer breakDurationMinutes();
    LocalTime earliestBreakTime();
    LocalTime latestBreakTime();
    Integer maxDrivingHoursWithoutBreak();
    Integer minDrivingBreakMinutes();

}
