package GoLogAPI.dto.driver;

import java.time.LocalDate;
import java.util.UUID;

import GoLogAPI.dto.user.UserResponse;

public record DriverResponse(
        UUID id,
        String name,
        String email,
        String cnhNumber,
        String cpf,
        LocalDate cnhExpiration,
        Double costPerHour,
        UserResponse user
) { }
