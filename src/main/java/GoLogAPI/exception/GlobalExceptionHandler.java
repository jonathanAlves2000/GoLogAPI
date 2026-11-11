package GoLogAPI.exception;

import GoLogAPI.service.MessageException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Converte toda exceção da API em {@link ApiError} com mensagem em português e status HTTP coerente.
 *
 * <p>Antes desta padronização, erros de regra ({@code ResponseStatusException}), JSON malformado,
 * enums inválidos e violações de chave estrangeira caíam no handler genérico e voltavam como
 * "Ocorreu um erro interno inesperado." (HTTP 500), escondendo do operador a causa real.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationDto(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : error.getField() + " inválido")
                .distinct()
                .toList();
        String message = errors.isEmpty() ? "Dados inválidos." : String.join(" • ", errors);
        return build(HttpStatus.BAD_REQUEST, message, errors);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex) {
        List<String> errors = ex.getErrors() == null ? List.of() : ex.getErrors();
        String message = errors.isEmpty() ? "Conflito com dados já cadastrados." : String.join(" • ", errors);
        return build(HttpStatus.CONFLICT, message, errors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : MessageException.NOT_FOUND_MESSAGE;
        if (ex.getId() != null) {
            message = message.trim() + " " + ex.getId();
        }
        return build(HttpStatus.NOT_FOUND, message, List.of());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex) {
        return build(ex.getStatus(), ex.getMessage(), List.of());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.BAD_REQUEST;
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return build(status, message, List.of());
    }

    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<ApiError> handleBadCredentials() {
        return build(HttpStatus.UNAUTHORIZED, MessageException.INVALID_CREDENTIALS_MESSAGE, List.of());
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ApiError> handleDisabled() {
        return build(HttpStatus.UNAUTHORIZED, "Usuário desativado. Procure o administrador do seu tenant.", List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied() {
        return build(HttpStatus.FORBIDDEN, "Seu perfil não tem permissão para executar esta operação.", List.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException ex) {
        String message = "Corpo da requisição inválido ou mal formatado.";
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            String field = fieldPath(ife);
            String expected = ife.getTargetType() != null && ife.getTargetType().isEnum()
                    ? " Valores aceitos: " + java.util.Arrays.stream(ife.getTargetType().getEnumConstants())
                        .map(Object::toString).collect(Collectors.joining(", ")) + "."
                    : "";
            message = "Valor '" + ife.getValue() + "' inválido para o campo '" + field + "'." + expected;
        } else if (cause instanceof MismatchedInputException mie) {
            message = "Formato inválido para o campo '" + fieldPath(mie) + "'.";
        }
        return build(HttpStatus.BAD_REQUEST, message, List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "Parâmetro '" + ex.getName() + "' com valor inválido: " + ex.getValue(), List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException ex) {
        return build(HttpStatus.BAD_REQUEST, "Parâmetro obrigatório ausente: " + ex.getParameterName(), List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethod(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP " + ex.getMethod() + " não suportado neste endpoint.", List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException ex) {
        String raw = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";
        log.warn("Violação de integridade: {}", raw);
        String message;
        if (raw != null && (raw.contains("foreign key") || raw.contains("chave estrangeira") || raw.contains("violates foreign key"))) {
            message = "Não é possível concluir a operação: este registro está vinculado a outros cadastros "
                    + "(ex.: remessas, escalas, conjuntos ou transportes). Remova ou altere os vínculos primeiro.";
        } else if (raw != null && (raw.contains("duplicate key") || raw.contains("unique"))) {
            message = "Já existe um registro com estes dados (valor duplicado).";
        } else if (raw != null && raw.contains("not-null")) {
            message = "Campo obrigatório não informado.";
        } else {
            message = "Os dados enviados violam uma regra de integridade do banco de dados.";
        }
        return build(HttpStatus.CONFLICT, message, List.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Argumento inválido", ex);
        return build(HttpStatus.BAD_REQUEST,
                ex.getMessage() != null ? ex.getMessage() : "Dados inválidos.", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneralException(Exception ex) {
        log.error("Erro não tratado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, MessageException.UNEXPECTED_ERROR_MESSAGE, List.of());
    }

    private static String fieldPath(MismatchedInputException ex) {
        String path = ex.getPath().stream()
                .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                .collect(Collectors.joining("."));
        return path.isBlank() ? "?" : path;
    }

    private static ResponseEntity<ApiError> build(HttpStatus status, String message, List<String> errors) {
        return ResponseEntity.status(status).body(ApiError.of(status.value(), message, errors));
    }
}
