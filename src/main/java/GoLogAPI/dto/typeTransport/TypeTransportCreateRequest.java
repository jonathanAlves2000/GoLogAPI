package GoLogAPI.dto.typeTransport;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record TypeTransportCreateRequest(
        @NotBlank(message = "O nome do tipo de transporte é obrigatório")

        @Pattern(
                regexp = "^[A-Z ]+$",
                message = "O nome deve conter apenas letras maiúsculas de A-Z (sem acentos) e espaços"
        )
        String name,

        String description,
        String care
) implements TypeTransportRequest { }
