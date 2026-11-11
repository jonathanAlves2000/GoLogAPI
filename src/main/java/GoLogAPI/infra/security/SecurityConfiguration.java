package GoLogAPI.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    private SecurityFilter securityFilter;
    private CouncorrencyLimiterFilter councorrencyLimiterFilter;

    public SecurityConfiguration(SecurityFilter securityFilter, CouncorrencyLimiterFilter councorrencyLimiterFilter){
        this.securityFilter = securityFilter;
        this.councorrencyLimiterFilter = councorrencyLimiterFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        return httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sesssion -> sesssion
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                		.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        // Gestão de usuários, tenants e tokens: somente ADMIN
                        .requestMatchers(HttpMethod.POST, "/login/powerbi-token").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/user", "/user/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/user", "/user/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/user", "/user/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/user", "/user/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/tenants", "/tenants/**").hasRole("ADMIN")
                        .requestMatchers("/auth-log", "/auth-log/**").hasRole("ADMIN")
                        // Motorista (app mobile): leitura geral + registro de ocorrências, telemetria e paradas
                        .requestMatchers(HttpMethod.GET, "/**").hasAnyRole("ADMIN", "DRIVER", "POWERBI", "OPERATOR")
                        .requestMatchers("/occurrence", "/occurrence/**", "/telemetry", "/telemetry/**",
                                "/api/v1/telemetry", "/api/v1/telemetry/**", "/route-stops", "/route-stops/**")
                                .hasAnyRole("ADMIN", "DRIVER", "OPERATOR")
                        // Demais escritas: backoffice
                        .requestMatchers(HttpMethod.POST, "/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers(HttpMethod.PUT, "/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers(HttpMethod.PATCH, "/**").hasAnyRole("ADMIN", "OPERATOR")
                        .requestMatchers(HttpMethod.DELETE, "/**").hasAnyRole("ADMIN", "OPERATOR")
                        .anyRequest().authenticated())
                
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(councorrencyLimiterFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> writeJson(res, 401,
                                "Sessão ausente ou expirada. Faça login novamente."))
                        .accessDeniedHandler((req, res, e) -> writeJson(res, 403,
                                "Seu perfil não tem permissão para executar esta operação.")))
                .build();
    }

    private static void writeJson(jakarta.servlet.http.HttpServletResponse res, int status, String message) throws java.io.IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"status\": " + status + ", \"message\": \"" + message + "\", \"errors\": []}");
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer(){
        return web -> {
            web.ignoring().requestMatchers(
                    "/v2/api-docs/**",
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/webjars/**"
            );
        };
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }

}
