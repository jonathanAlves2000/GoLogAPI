package GoLogAPI.exception;

import java.util.ArrayList;
import java.util.List;

public class ConflictException extends RuntimeException {

    private List<String> errors;

    public ConflictException(List<String> errorList){
        super("Múltiplos erros de validação!");
        this.errors = new ArrayList<>(errorList);
        errorList.forEach(messageError -> System.err.println(messageError));
        errorList.clear();
    }

    public List<String> getError(){
        return errors;
    }
}
