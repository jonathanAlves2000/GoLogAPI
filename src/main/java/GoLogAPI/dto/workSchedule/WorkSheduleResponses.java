package GoLogAPI.dto.workSchedule;

import GoLogAPI.model.Driver;
import GoLogAPI.model.EquipamentGroup;
import GoLogAPI.model.enums.WorkScheduleStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record WorkSheduleResponses(
        UUID id,
        //@JsonIgnoreProperties("user")
        Driver driver,
        //@JsonIgnoreProperties("company")
        EquipamentGroup equipamentGroup,
        LocalDate scheduleDate,
        LocalTime startWorkday,
        LocalTime endWorkday,
        WorkScheduleStatus status
) { }
