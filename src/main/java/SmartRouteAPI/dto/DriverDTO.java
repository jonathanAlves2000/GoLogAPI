package SmartRouteAPI.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DriverDTO {

    @NotBlank(message = "O nome é obrigatório")
    private String name;

    @NotBlank(message = "O CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 digitos numéricos")
    private String cpf;

    @NotBlank(message = "A CNH é obrigatória")
    private String cnh;

    @NotBlank(message = "O número de celular é obrigatório")
    @Pattern(regexp = "\\+?\\d{10,15}", message = "Número de celular invalido")
    private String cellNumer;

    private Integer userId;
}
