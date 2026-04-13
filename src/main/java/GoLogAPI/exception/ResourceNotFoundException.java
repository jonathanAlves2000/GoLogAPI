package GoLogAPI.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException{
	
	private static final long serialVersionUID = 1L;

    private final UUID id;

    public ResourceNotFoundException(String message, UUID id){
        super(message);
        this.id = id;
    }

    public UUID getId() {
        return id;
    }
}
