package GoLogAPI.dto.driver;

import java.time.LocalDate;
import java.util.UUID;

public record DriverResponseList(
        UUID id,
        String name,
        String email,
        String cnhNumber,
        String cpf,
        LocalDate cnhExpiration
) { }
