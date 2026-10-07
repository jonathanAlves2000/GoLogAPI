package GoLogAPI.dto.webhook;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record WebhookConfigRequest(
        @NotBlank(message = "A URL de webhook deve ser informada.")
        @URL(message = "Formato de URL inválido.")
        String webhookUrl,

        String webhookSecret,

        Boolean webhookActive
) { }
