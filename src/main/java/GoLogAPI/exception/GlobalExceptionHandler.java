package GoLogAPI.exception;

import GoLogAPI.dto.exeption.ConflictReponse;
import GoLogAPI.dto.exeption.NotFoundResponse;
import GoLogAPI.dto.exeption.AuthErrorReponse;
import GoLogAPI.service.MessageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<String>> handleValidationDto(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .toList();
        return ResponseEntity
                .badRequest()
                .body(errors);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ConflictReponse> handleConflict(ConflictException exception) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ConflictReponse(exception.getErrors()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<NotFoundResponse> handleNotFound(ResourceNotFoundException exception) {
        var responseBody = new NotFoundResponse(
                exception.getMessage(),
                exception.getId()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(responseBody);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AuthErrorReponse> handleBadCredentials() {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new AuthErrorReponse(MessageException.INVALID_CREDENTIALS_MESSAGE));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception exception) {
        exception.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(MessageException.UNEXPECTED_ERROR_MESSAGE);
    }
}