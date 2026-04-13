package GoLogAPI.exception;

import java.util.ArrayList;
import java.util.List;

public class ConflictException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;

    private List<String> errors;

    public ConflictException(List<String> errorList){
        super();
        this.errors = new ArrayList<>(errorList);
        errorList.clear();
    }

    public List<String> getErrors(){
        return errors;
    }
}
