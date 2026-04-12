package GoLogAPI.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException{

    private final UUID id;

    public ResourceNotFoundException(String message, UUID id){
        super(message);
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
