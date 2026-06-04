package GoLogAPI.dto.tractor;


import GoLogAPI.model.Company;
import GoLogAPI.model.TypeFuel;

import java.util.UUID;

public interface TractorRequest {
    String plate();
    String renavam();
    String model();
    Integer numberAxles();
    Double maximumCapacity();
    TypeFuel typeFuel();
    Double kmPerLiter();
    UUID companyId();
}
