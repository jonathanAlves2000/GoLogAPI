package GoLogAPI.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import GoLogAPI.dto.login.AuthLogResponse;
import GoLogAPI.service.AuthLogService;

@RestController
@RequestMapping("/auth-log")
public class AuthLogController {

    private final AuthLogService authLogService;

    public AuthLogController(AuthLogService authLogService){
        this.authLogService = authLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuthLogResponse>> getAll(){
        List<AuthLogResponse> authLogResponses = authLogService.getAll();
        return ResponseEntity.status(200).body(authLogResponses);
    }

}
