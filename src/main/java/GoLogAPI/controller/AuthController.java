package GoLogAPI.controller;

import GoLogAPI.dto.login.LoginRequest;
import GoLogAPI.dto.login.PowerBiTokenResponse;
import GoLogAPI.dto.login.TokenResponse;
import GoLogAPI.infra.security.TokenService;
import GoLogAPI.model.AuthLog;
import GoLogAPI.model.User;
import GoLogAPI.service.AuthLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@Tag(name = "Autenticação")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final AuthLogService authLogService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService, AuthLogService authLogService){
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.authLogService = authLogService;
    }

    @Operation(summary = "Autenticar", description = "Realiza a autenticação do usuário e retorna o token JWT")
    @PostMapping
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(loginRequest.email() ,loginRequest.password());
        var authentication = authenticationManager.authenticate(authenticationToken);
        var tokenJWT = tokenService.createToken((User) authentication.getPrincipal());

        User user = (User) authentication.getPrincipal();
        authLogService.save(user);

        UUID companyId = user.getCompany() != null ? user.getCompany().getId() : null;
        String companyName = user.getCompany() != null ? user.getCompany().getLegalName() : null;
        String companyType = (user.getCompany() != null && user.getCompany().getCompanyType() != null)
                ? user.getCompany().getCompanyType().name() : null;
        Boolean isMaster = user.getCompany() != null && Boolean.TRUE.equals(user.getCompany().getIsMaster());

        return ResponseEntity.ok(new TokenResponse(
                tokenJWT,
                user.getId(),
                user.getName(),
                user.getUserProfile().name(),
                companyId,
                companyName,
                companyType,
                isMaster
        ));
    }

    @Operation(summary = "Gerar token Power BI", description = "Gera um token JWT sem expiração para consumo via Power BI. Requer role ADMIN.")
    @PostMapping("/powerbi-token")
    public ResponseEntity<PowerBiTokenResponse> generatePowerBiToken() {
        var tokenJWT = tokenService.createPowerBiToken();
        return ResponseEntity.ok(new PowerBiTokenResponse(tokenJWT));
    }
}
