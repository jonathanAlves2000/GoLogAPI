package GoLogAPI.dto.user;

import GoLogAPI.dto.company.CompanyResponse;
import GoLogAPI.model.UserProfile;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String password,
        String cpf,
        UserProfile userProfile,
        CompanyResponse company
) { }
