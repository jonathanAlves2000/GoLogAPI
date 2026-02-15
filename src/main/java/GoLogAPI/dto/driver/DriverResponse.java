package GoLogAPI.dto.driver;

import GoLogAPI.dto.user.UserResponse;

import java.time.LocalDate;
import java.util.UUID;

public record DriverResponse(
        UUID id,
        String cnhNumber,
        LocalDate cnhExpiration,
        UserResponse user
) { }
