package GoLogAPI.infra.security;

import GoLogAPI.repository.UserRepository;
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

    private TokenService tokenService;
    private UserRepository userRepository;
    private GetUserDetailsService getUserDetailsService;

    public SecurityFilter(TokenService tokenService, UserRepository userRepository, GetUserDetailsService getUserDetailsService){
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.getUserDetailsService = getUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

            var token = recoverToken(request);

            if(token != null){
                var subject = tokenService.getSubject(token);
                var userDetails = getUserDetailsService.loadUserByUsername(subject);
                var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request){
        var authorizationHeader = request.getHeader("Authorization");
        if(authorizationHeader != null){
            return authorizationHeader.replace("Bearer ", "").trim();
        }
        return null;
    }
}
