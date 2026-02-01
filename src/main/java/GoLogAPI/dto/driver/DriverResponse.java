package GoLogAPI.dto.driver;

import GoLogAPI.dto.user.UserResponse;
import GoLogAPI.model.User;

import java.time.LocalDate;
import java.util.UUID;

public record DriverResponse(
        UUID id,
        String cnhNumber,
        LocalDate cnhExpiration,
        UserResponse user
) { }
