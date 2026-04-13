package GoLogAPI.dto.user;

import GoLogAPI.model.UserProfile;
import java.util.UUID;

public interface UserRequest {
    String name();
    String email();
    String password();
    String cpf();
    UserProfile userProfile();
    UUID companyId();
}
