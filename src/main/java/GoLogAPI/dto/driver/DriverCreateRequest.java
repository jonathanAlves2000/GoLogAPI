package GoLogAPI.dto.driver;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

public record DriverCreateRequest(

        @NotBlank(message = "CNH deve ser informado")
        @Pattern(regexp = "\\+?\\d{11}", message = "CNH invalida, minimo 11 digitos")
        String cnhNumber,

        @NotNull(message = "Data de expiração da CNH deve ser informada.")
        @JsonFormat(pattern = "dd/MM/yyyy")
        @Future(message = "A data de expiração deve ser maior que a data atual")
        LocalDate cnhExpiration,

        @NotNull(message = "Id do usuario deve ser informado.")
        UUID userId
) implements DriverRequest { }
