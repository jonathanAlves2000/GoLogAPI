package GoLogAPI.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

    private List<String> error;

    public ConflictException(List<String> errorList){
        super("Múltiplos erros de validação!");
        this.error = errorList;
        errorList.forEach(messageError -> System.err.println(messageError));
        errorList.clear();
    }

    public List<String> getError(){
        return error;
    }
}
