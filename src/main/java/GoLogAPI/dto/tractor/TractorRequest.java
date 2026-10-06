package GoLogAPI.dto.tractor;


import java.util.UUID;

public interface TractorRequest {
    String plate();
    String renavam();
    String model();
    Integer numberAxles();
    Double maximumCapacity();
    Double costPerKilometer();
    UUID companyId();
}
