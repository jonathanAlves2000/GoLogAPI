package GoLogAPI.dto.driver;

import GoLogAPI.dto.user.UserResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record DriverResponse(
        UUID id,
        String name,
        String email,
        String cnhNumber,
        String cpf,
        LocalDate cnhExpiration,
        UserResponse user
) { }
