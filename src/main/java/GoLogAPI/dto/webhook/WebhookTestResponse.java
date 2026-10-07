package GoLogAPI.dto.webhook;

public record WebhookTestResponse(
        boolean success,
        int statusCode,
        String responseBody,
        long latencyMs,
        String message
) { }
