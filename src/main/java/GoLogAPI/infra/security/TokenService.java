package GoLogAPI.infra.security;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import GoLogAPI.model.User;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String createToken(User user){
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("golog-api")
                    .withSubject(user.getEmail())
                    .withClaim("userProfile", user.getUserProfile().name())
                    .withExpiresAt(dateExpiration())
                    .sign(algorithm);
        }catch (JWTCreationException exception){
            throw new RuntimeException("Erro ao gerar token jwt", exception);
        }
    }

    private Instant dateExpiration(){
        return LocalDateTime
                .now()
                .plusMinutes(60)
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

}
