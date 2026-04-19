package GoLogAPI.controller;

import GoLogAPI.dto.login.LoginRequest;
import GoLogAPI.dto.login.TokenResponse;
import GoLogAPI.infra.security.TokenService;
import GoLogAPI.model.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService){
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.email() ,loginRequest.password());
        var authentication = authenticationManager.authenticate(authenticationToken);
        var tokenJWT = tokenService.createToken((User) authentication.getPrincipal());

        return ResponseEntity.ok(new TokenResponse(tokenJWT));
    }
}
