package GoLogAPI.service;

import GoLogAPI.model.AuthLog;
import GoLogAPI.model.User;
import GoLogAPI.repository.AuthLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthLogService {

    private final AuthLogRepository authLogRepository;

    public AuthLogService(AuthLogRepository authLogRepository){
        this.authLogRepository = authLogRepository;
    }

    public void save(User user){
        AuthLog authLog = new AuthLog();
        authLog.setCreatedBy(user.getName());
        authLogRepository.save(authLog);
    }

}
