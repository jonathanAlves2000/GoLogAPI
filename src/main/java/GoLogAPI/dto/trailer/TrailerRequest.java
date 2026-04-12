package GoLogAPI.dto.trailer;

public interface TrailerRequest {
    String plate();
    String renavam();
    String model();
    Integer numberAxles();
    Double maximumCapacity();
    Double maximumVolume();
}
