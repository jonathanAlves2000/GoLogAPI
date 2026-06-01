package GoLogAPI.infra.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RouteOptimizationClient {

    private final RestClient restClient;

    public RouteOptimizationClient(RestClient.Builder restClientBuilder,
                                   @Value("${api.route-optimization.url}") String baseUrl,
                                   @Value("${api.route-optimization.token}") String token,
                                   ObjectMapper objectMapper)
    {

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-Goog-Api-Key", "Bearer " + token)
                .build();
    }

    public String fetchOptimizedRoute(Object requestPayload) {
        try {
            return this.restClient.post()
                    .uri("/projects/stately-arbor-495218-n1:optimizeTours")
                    .body(requestPayload)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao comunicar com a API de Otimização: " + e.getMessage(), e);
        }
    }
}
