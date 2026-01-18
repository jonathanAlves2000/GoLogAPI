package GoLogAPI.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DriverDto {

    @NotBlank(message = "Campo numero da CNH nulo")
    private String cnhNumber;

    @NotBlank(message = "Campo data de expiração da CNH nulo")
    @Pattern(regexp = "\\+?\\d{8}", message = "Formato de data invalida")
    private String cnhExpiration;

    @NotNull(message = "Campo id nulo")
    private Integer userId;
}
