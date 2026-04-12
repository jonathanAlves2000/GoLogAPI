package GoLogAPI.dto.tractor;


public interface TractorRequest {
    String plate();
    String renavam();
    String model();
    Integer numberAxles();
    Double maximumCapacity();
    String typeFuel();
}
