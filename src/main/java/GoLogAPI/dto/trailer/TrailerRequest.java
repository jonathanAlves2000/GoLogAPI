package GoLogAPI.dto.trailer;

import java.util.UUID;

public interface TrailerRequest {
    String plate();
    String renavam();
    String model();
    Integer numberAxles();
    Double maximumCapacity();
    Double maximumVolume();
    UUID companyId();
}
