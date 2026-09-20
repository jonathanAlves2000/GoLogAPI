package GoLogAPI.infra.security;

import GoLogAPI.model.PowerBiToken;
import GoLogAPI.model.User;
import GoLogAPI.model.UserProfile;
import GoLogAPI.repository.PowerBiRepository;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    private final PowerBiRepository powerBiRepository;

    public TokenService(PowerBiRepository powerBiRepository){
        this.powerBiRepository = powerBiRepository;
    }

    public String createToken(User user){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("golog-api")
                    .withSubject(user.getEmail())
                    .withClaim("user", user.getName())
                    .withClaim("role", user.getUserProfile().name())
                    .withExpiresAt(dateExpiration())
                    .sign(algorithm);
        }catch (JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar token jwt", exception);
        }
    }

    private Instant dateExpiration(){
        return LocalDateTime
                .now()
                .plusMinutes(120)
                .toInstant(ZoneOffset.of("-03:00"));
    }

    public String getSubject(String tokenJWT) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.require(algorithm)
                .withIssuer("golog-api")
                .build()
                .verify(tokenJWT)
                .getSubject();
    }

    public String getClaim(String tokenJWT, String claimName) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.require(algorithm)
                .withIssuer("golog-api")
                .build()
                .verify(tokenJWT)
                .getClaim(claimName)
                .asString();
    }

    public String createPowerBiToken(){
        try {
            String jwtid = UUID.randomUUID().toString();

            Algorithm algorithm = Algorithm.HMAC256(secret);
            String token = JWT.create()
                    .withIssuer("golog-api")
                    .withSubject("powerbi-service")
                    .withClaim("role", UserProfile.POWERBI.name())
                    .withJWTId(jwtid)
                    .sign(algorithm);

            PowerBiToken powerBiToken = PowerBiToken.builder()
                    .jwtId(jwtid)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            powerBiRepository.save(powerBiToken);

            return token;
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Erro ao gerar token JWT do Power BI", exception);
        }
    }

    public Boolean isPowerBiTokenValid(String tokenJWT){
        Algorithm algorithm = Algorithm.HMAC256(secret);
        String jwtId = JWT.require(algorithm)
                .withIssuer("golog-api")
                .build()
                .verify(tokenJWT)
                .getId();

        return powerBiRepository.findByJwtIdAndActiveTrue(jwtId).isPresent();
    }

}
