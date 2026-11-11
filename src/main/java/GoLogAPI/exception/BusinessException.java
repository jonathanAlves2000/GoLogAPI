package GoLogAPI.exception;

import org.springframework.http.HttpStatus;

/**
 * Violação de regra de negócio com status HTTP explícito e mensagem pronta para o usuário final.
 * Tratada pelo {@link GlobalExceptionHandler}, que a converte em {@link ApiError}.
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
