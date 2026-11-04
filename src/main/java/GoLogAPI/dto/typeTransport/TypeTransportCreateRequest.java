package GoLogAPI.dto.typeTransport;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record TypeTransportCreateRequest(
        @NotBlank(message = "O nome do tipo de transporte é obrigatório")

        @Pattern(
                regexp = "^[A-Za-z0-9À-ÿ _-]+$",
                message = "O nome deve conter apenas letras, números, espaços, hífen ou underline"
        )
        String name,

        String description,
        String care,
        UUID companyId
) implements TypeTransportRequest {
    public TypeTransportCreateRequest(String name, String description, String care) {
        this(name, description, care, null);
    }
}

