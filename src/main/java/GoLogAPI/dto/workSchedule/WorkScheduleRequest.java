package GoLogAPI.dto.workSchedule;

import GoLogAPI.model.enums.WorkScheduleStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public interface WorkScheduleRequest {

    UUID driverId();
    UUID equipamentGroupId();
    LocalDate scheduleDate();
    WorkScheduleStatus status();
    LocalTime startWorkday();
    LocalTime endWorkday();

}
