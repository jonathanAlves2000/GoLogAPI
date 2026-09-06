package GoLogAPI.dto.driver;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public interface DriverRequest {
    String cnhNumber();
    LocalDate cnhExpiration();
    Double costPerHour();
    UUID userId();
}
