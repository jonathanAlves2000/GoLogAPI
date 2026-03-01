package GoLogAPI.dto.user;

import GoLogAPI.model.UserProfile;

import java.util.UUID;

public record UserResponseList(
        UUID id,
        String name,
        String email,
        String password,
        String cpf,
        UserProfile userProfile
) { }
