package GoLogAPI.exception;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String message){
        super(message);
        System.err.println(message);
    }
}
