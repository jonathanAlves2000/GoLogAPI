package GoLogAPI.dto.driver;

import java.time.LocalDate;
import java.util.UUID;

public interface DriverRequest {
    String cnhNumber();
    LocalDate cnhExpiration();
    UUID userId();
}
