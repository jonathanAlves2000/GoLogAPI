package GoLogAPI.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DriverDTO {

    @NotBlank(message = "A CNH é obrigatória")
    private String cnhNumber;

    @NotBlank(message = "A data de expiração da cnh é pbrigatória")
    @Pattern(regexp = "\\+?\\d{8}", message = "Formato da data invalida")
    private String cnhExpiration;

    private Integer userId;
}
