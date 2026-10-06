package GoLogAPI.infra.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
	
	@Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost*", "http://127.0.0.1*", "http://147.15.18.21*", "https://147.15.18.21*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD", "TRACE", "CONNECT")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
