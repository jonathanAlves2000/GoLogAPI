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
                        .requestMatchers(HttpMethod.POST, "/user").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/user").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/user").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/login/powerbi-token").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/**").hasAnyRole("ADMIN", "DRIVER", "POWERBI", "OPERATOR")
                        .requestMatchers(HttpMethod.POST, "/**").hasAnyRole("ADMIN", "DRIVER", "OPERATOR")
                        .requestMatchers(HttpMethod.PUT, "/**").hasAnyRole("ADMIN", "DRIVER", "OPERATOR")
                        .requestMatchers(HttpMethod.PATCH, "/**").hasAnyRole("ADMIN", "DRIVER", "OPERATOR")
                        .requestMatchers(HttpMethod.DELETE, "/**").hasAnyRole("ADMIN", "DRIVER", "OPERATOR")
                        .anyRequest().authenticated())
                
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(councorrencyLimiterFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
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
