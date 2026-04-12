package GoLogAPI.infra.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final GetUserDetailsService getUserDetailsService;

    public SecurityFilter(TokenService tokenService, GetUserDetailsService getUserDetailsService){
        this.tokenService = tokenService;
        this.getUserDetailsService = getUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

            var token = recoverToken(request);

            if(token != null){
                try {
                    var subject = tokenService.getSubject(token);
                    var userDetails = getUserDetailsService.loadUserByUsername(subject);
                    var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (TokenExpiredException exception) {
                    sendError(response, "Token expirado. Por favor, faça login novamente.");
                    return;
                } catch (JWTVerificationException exception) {
                    sendError(response, "Token inválido ou malformatado.");
                    return;
                }
            }

            filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, String mensagem) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"erro\": \"" + mensagem + "\"}");
    }

    private String recoverToken(HttpServletRequest request){
        var authorizationHeader = request.getHeader("Authorization");
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            return authorizationHeader.replace("Bearer ", "").trim();
        }
        return null;
    }
}
