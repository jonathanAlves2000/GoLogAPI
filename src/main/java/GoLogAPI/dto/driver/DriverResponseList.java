package GoLogAPI.dto.driver;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record DriverResponseList(
        UUID id,
        String name,
        String email,
        String cnhNumber,
        String cpf,
        LocalTime startWorkday,
        LocalTime endWorkday,
        LocalDate cnhExpiration
) { }
