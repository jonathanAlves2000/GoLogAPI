package GoLogAPI.infra.client;

import GoLogAPI.infra.config.GoogleAuthConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

@Component
public class RouteOptimizationClient {

    private final RestClient restClient;
    private final GoogleAuthConfig googleAuthConfig;

    public RouteOptimizationClient(
            RestClient.Builder restClientBuilder,
            @Value("${api.route-optimization.url}") String baseUrl,
            ObjectMapper objectMapper,
            GoogleAuthConfig googleAuthConfig) {

        this.googleAuthConfig = googleAuthConfig;

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String fetchOptimizedRoute(Object requestPayload) {

        try {

            String accessToken = googleAuthConfig.getAccessToken();

            return this.restClient.post()
                    .uri("/projects/stately-arbor-495218-n1:optimizeTours")
                    .header("Authorization", "Bearer " + accessToken)
                    .body(requestPayload)
                    .retrieve()
                    .body(String.class);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Erro ao comunicar com a API de Otimização: "
                            + e.getMessage(),
                    e
            );
        }
    }
}
