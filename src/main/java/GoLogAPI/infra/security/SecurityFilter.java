package GoLogAPI.infra.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import GoLogAPI.infra.tenant.TenantContext;
import java.util.UUID;

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
                var email = tokenService.getSubject(token);
                var role = tokenService.getClaim(token, "role");

                if("POWERBI".equals(role) && !tokenService.isPowerBiTokenValid(token)){
                    sendError(response, "Token do Power BI revogado ou invalido.");
                    return;
                }

                var authority = new SimpleGrantedAuthority(role != null && role.startsWith("ROLE_") ? role : "ROLE_" + role);
                var authentication = new UsernamePasswordAuthenticationToken(email, null, List.of(authority));
                SecurityContextHolder.getContext().setAuthentication(authentication);

                String companyIdClaim = tokenService.getClaim(token, "companyId");
                Boolean isMasterClaim = tokenService.getBooleanClaim(token, "isMaster");
                String companyTypeClaim = tokenService.getClaim(token, "companyType");

                UUID userCompanyId = companyIdClaim != null ? UUID.fromString(companyIdClaim) : null;
                boolean isMaster = Boolean.TRUE.equals(isMasterClaim);

                TenantContext.setUserCompanyId(userCompanyId);
                TenantContext.setMaster(isMaster);
                TenantContext.setCompanyType(companyTypeClaim);

                String headerTenant = request.getHeader("X-Tenant-Id");
                if (isMaster && headerTenant != null && !headerTenant.isBlank() && !"all".equalsIgnoreCase(headerTenant)) {
                    try {
                        TenantContext.setCurrentTenantId(UUID.fromString(headerTenant.trim()));
                    } catch (IllegalArgumentException e) {
                        TenantContext.setCurrentTenantId(null);
                    }
                } else if (isMaster) {
                    TenantContext.setCurrentTenantId(null);
                } else {
                    TenantContext.setCurrentTenantId(userCompanyId);
                }

            } catch (TokenExpiredException exception) {
                sendError(response, "Token expirado. Por favor, faça login novamente.");
                return;
            } catch (JWTVerificationException exception) {
                sendError(response, "Token inválido ou malformatado.");
                return;
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
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
