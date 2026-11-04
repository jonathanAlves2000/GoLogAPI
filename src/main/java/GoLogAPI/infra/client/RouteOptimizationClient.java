package GoLogAPI.infra.client;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.maps.routeoptimization.v1.OptimizeToursRequest;
import com.google.maps.routeoptimization.v1.OptimizeToursResponse;
import com.google.maps.routeoptimization.v1.RouteOptimizationSettings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Cliente da Google Route Optimization API (routeoptimization.googleapis.com).
 * Substitui a antiga Cloud Optimization API (FleetRouting), que foi descontinuada.
 */
@Component
public class RouteOptimizationClient {

    private final String projectId;
    private com.google.maps.routeoptimization.v1.RouteOptimizationClient googleClient;

    public RouteOptimizationClient(@Value("${google.cloud.project-id:stately-arbor-495218-n1}") String projectId) {
        this.projectId = projectId;
    }

    private synchronized com.google.maps.routeoptimization.v1.RouteOptimizationClient getClient() {
        if (this.googleClient == null) {
            try (FileInputStream credentialsStream = new FileInputStream("ApiRouteOptimization.json")) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                        .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

                RouteOptimizationSettings settings = RouteOptimizationSettings.newBuilder()
                        .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                        .build();

                this.googleClient = com.google.maps.routeoptimization.v1.RouteOptimizationClient.create(settings);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao inicializar Google RouteOptimizationClient: " + e.getMessage(), e);
            }
        }
        return this.googleClient;
    }

    public OptimizeToursResponse optimizeTours(OptimizeToursRequest request) {
        try {
            return getClient().optimizeTours(request);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao comunicar com a API de Otimização de Rotas do Google: " + e.getMessage(), e);
        }
    }

    public String getProjectId() {
        return projectId;
    }

    @PreDestroy
    public void cleanup() {
        if (googleClient != null && !googleClient.isShutdown()) {
            googleClient.shutdown();
        }
    }
}
