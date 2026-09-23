package GoLogAPI.service;

import GoLogAPI.dto.login.AuthLogResponse;
import GoLogAPI.model.AuthLog;
import GoLogAPI.model.User;
import GoLogAPI.repository.AuthLogRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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

    public List<AuthLogResponse> getAll(){

        List<AuthLog> authLogList = authLogRepository.findAll();

        return authLogList.stream()
                .map(authLog ->  new AuthLogResponse(
                        authLog.getId(),
                        authLog.getCreatedAt(),
                        authLog.getCreatedBy(),
                        authLog.getUpdatedAt(),
                        authLog.getUpdatedBy()
                )).toList();
    }

}
