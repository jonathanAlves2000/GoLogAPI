package GoLogAPI.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageException {
    public static String NOT_FOUND_MESSAGE = "Registro não encontrado para o Id: ";
    public static String INVALID_CREDENTIALS_MESSAGE = "E-mail ou senha invalidos";
}
