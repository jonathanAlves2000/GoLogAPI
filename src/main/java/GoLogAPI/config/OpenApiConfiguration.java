package GoLogAPI.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "GoLog API",
                version = "1.0.0",
                contact = @Contact(
                        name = "Jonatthan Alves",
                        email = "jonathanmatheusalves@gmail.com",
                        url = "gologapi.com"
                )
        )
)
public class OpenApiConfiguration {

}
