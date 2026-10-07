package GoLogAPI.service.webhook;

import GoLogAPI.dto.webhook.WebhookTestResponse;
import GoLogAPI.model.Company;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class WebhookDispatcherService {

    private static final Logger log = LoggerFactory.getLogger(WebhookDispatcherService.class);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public WebhookDispatcherService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    /**
     * Dispara um webhook assíncrono para o ERP do cliente cadastrado na Company.
     */
    public CompletableFuture<Void> dispatch(Company company, String eventType, Object eventData) {
        if (company == null || company.getWebhookUrl() == null || company.getWebhookUrl().isBlank()) {
            return CompletableFuture.completedFuture(null);
        }

        if (Boolean.FALSE.equals(company.getWebhookActive())) {
            log.debug("Webhook desativado para a empresa {}", company.getLegalName());
            return CompletableFuture.completedFuture(null);
        }

        String url = company.getWebhookUrl().trim();
        String secret = company.getWebhookSecret();
        UUID companyId = company.getId();

        return CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> envelope = new HashMap<>();
                envelope.put("event", eventType);
                envelope.put("timestamp", Instant.now().toString());
                envelope.put("companyId", companyId);
                envelope.put("data", eventData);

                String jsonPayload = objectMapper.writeValueAsString(envelope);

                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .header("User-Agent", "GoLog-Webhook-Dispatcher/2.0")
                        .header("X-GoLog-Event", eventType)
                        .header("X-GoLog-Delivery", UUID.randomUUID().toString())
                        .header("X-GoLog-Timestamp", Instant.now().toString())
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8));

                if (secret != null && !secret.isBlank()) {
                    String signature = computeHmacSha256(jsonPayload, secret);
                    requestBuilder.header("X-GoLog-Signature", "sha256=" + signature);
                }

                HttpRequest request = requestBuilder.build();
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    log.info("Webhook [{}] entregue com sucesso para {}: HTTP {}", eventType, url, response.statusCode());
                } else {
                    log.warn("Webhook [{}] retornou status não-2xx para {}: HTTP {}", eventType, url, response.statusCode());
                }

            } catch (Exception e) {
                log.error("Falha ao entregar webhook [{}] para {}: {}", eventType, url, e.getMessage());
            }
        });
    }

    /**
     * Executa teste síncrono de conectividade com a URL de webhook informada.
     */
    public WebhookTestResponse testWebhook(String webhookUrl, String webhookSecret) {
        long startTime = System.currentTimeMillis();
        try {
            Map<String, Object> testPayload = new HashMap<>();
            testPayload.put("event", "TEST_PING");
            testPayload.put("timestamp", Instant.now().toString());
            testPayload.put("message", "Conexão de webhook verificada com sucesso pelo GoLog Middleware.");

            String jsonPayload = objectMapper.writeValueAsString(testPayload);

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(webhookUrl.trim()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "GoLog-Webhook-Dispatcher/2.0")
                    .header("X-GoLog-Event", "TEST_PING")
                    .header("X-GoLog-Delivery", UUID.randomUUID().toString())
                    .header("X-GoLog-Timestamp", Instant.now().toString())
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8));

            if (webhookSecret != null && !webhookSecret.isBlank()) {
                String signature = computeHmacSha256(jsonPayload, webhookSecret);
                requestBuilder.header("X-GoLog-Signature", "sha256=" + signature);
            }

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long latency = System.currentTimeMillis() - startTime;

            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
            String msg = success ? "Conexão estabelecida com sucesso." : "O endpoint de destino respondeu com erro HTTP " + response.statusCode();

            return new WebhookTestResponse(
                    success,
                    response.statusCode(),
                    response.body(),
                    latency,
                    msg
            );

        } catch (Exception e) {
            long latency = System.currentTimeMillis() - startTime;
            return new WebhookTestResponse(
                    false,
                    0,
                    null,
                    latency,
                    "Falha ao conectar no endpoint: " + e.getMessage()
            );
        }
    }

    private String computeHmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hmac);
        } catch (Exception e) {
            log.error("Erro ao calcular HMAC SHA-256 do webhook: {}", e.getMessage());
            return "";
        }
    }
}
