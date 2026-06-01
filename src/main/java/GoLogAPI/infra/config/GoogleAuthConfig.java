package GoLogAPI.infra.config;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Component
public class GoogleAuthConfig {
    public String getAccessToken() throws IOException {

        GoogleCredentials credentials =
                GoogleCredentials.fromStream(
                        new FileInputStream("ApiRouteOptimization.json")
                ).createScoped(
                        List.of("https://www.googleapis.com/auth/cloud-platform")
                );

        credentials.refreshIfExpired();

        return credentials.getAccessToken().getTokenValue();
    }
}
