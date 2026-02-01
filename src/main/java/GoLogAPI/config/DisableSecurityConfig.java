package GoLogAPI.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class DisableSecurityConfig {

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                // Desativa proteção contra CSRF (Cross-Site Request Forgery)
//                .csrf(csrf -> csrf.disable())
//
//                // Define regras de autorização para requisições HTTP
//                .authorizeHttpRequests(auth -> auth
//                        // Permite acesso irrestrito ao console H2
//                        .requestMatchers("/h2-console/**").permitAll()
//                        // Permite acesso irrestrito a qualquer outra requisição
//                        .anyRequest().permitAll()
//                )
//
//                // Desativa todos os headers de segurança, incluindo frameOptions
//                // Necessário para que o console H2 funcione corretamente em iframe
//                .headers(headers -> headers.disable());
//
//        // Retorna a cadeia de filtros configurada
//        return http.build();
//    }


}