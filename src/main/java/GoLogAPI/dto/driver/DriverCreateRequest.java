package GoLogAPI.dto.driver;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

public record DriverCreateRequest(

        @NotBlank(message = "Campo numero da CNH vazio")
        @Pattern(regexp = "\\+?\\d{11}", message = "CNH invalida, minimo 11 digitos")
        String cnhNumber,

        @NotNull(message = "Campo data de expiração da CNH vazio ou nulo")
        @JsonFormat(pattern = "dd/MM/yyyy")
        @Future(message = "A data de expiração deve ser maior que hoje")
        LocalDate cnhExpiration,

        @NotNull(message = "Id do usuario está vazio ou nulo")
        UUID userId
) implements DriverRequest { }
