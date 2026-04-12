package GoLogAPI.dto.driver;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

public record DriverUpdateRequest(

        @Pattern(regexp = "\\+?\\d{11}", message = "CNH invalida, minimo 11 digitos")
        String cnhNumber,

        @JsonFormat(pattern = "dd/MM/yyyy")
        @Future(message = "A data de expiração deve ser maior que hoje")
        LocalDate cnhExpiration,

        UUID userId
) implements DriverRequest { }
