package GoLogAPI.infra.client;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.optimization.v1.FleetRoutingClient;
import com.google.cloud.optimization.v1.FleetRoutingSettings;
import com.google.cloud.optimization.v1.OptimizeToursRequest;
import com.google.cloud.optimization.v1.OptimizeToursResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Component
public class RouteOptimizationClient {

    private final String projectId;
    private FleetRoutingClient fleetRoutingClient;

    public RouteOptimizationClient(@Value("${google.cloud.project-id:stately-arbor-495218-n1}") String projectId) {
        this.projectId = projectId;
    }

    private synchronized FleetRoutingClient getClient() {
        if (this.fleetRoutingClient == null) {
            try {
                GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("ApiRouteOptimization.json"))
                        .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

                FleetRoutingSettings settings = FleetRoutingSettings.newBuilder()
                        .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                        .build();

                this.fleetRoutingClient = FleetRoutingClient.create(settings);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao inicializar Google FleetRoutingClient: " + e.getMessage(), e);
            }
        }
        return this.fleetRoutingClient;
    }

    public OptimizeToursResponse optimizeTours(OptimizeToursRequest request) {
        try {
            return getClient().optimizeTours(request);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao comunicar com a API de Otimização gRPC do Google: " + e.getMessage(), e);
        }
    }

    public String getProjectId() {
        return projectId;
    }

    @PreDestroy
    public void cleanup() {
        if (fleetRoutingClient != null && !fleetRoutingClient.isShutdown()) {
            fleetRoutingClient.shutdown();
        }
    }
}
