package GoLogAPI.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import GoLogAPI.dto.driver.DriverRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.DriverRepository;

@Component
public class DriverValidator {

    private final DriverRepository driverRepository;

    public DriverValidator(DriverRepository driverRepository){
        this.driverRepository = driverRepository;
    }

    public void validate(DriverRequest driverRequest){
        List<String> errors = new ArrayList<>();

       if(driverRequest.cnhNumber() != null && !driverRequest.cnhNumber().isBlank())
         cnh(driverRequest.cnhNumber(), errors);

        if(!errors.isEmpty()) throw new ConflictException(errors);
    }

    public void cnh(String cnh, List<String> errors){
        boolean exists = driverRepository.existsByCnhNumber(cnh);
        if(exists) errors.add("CNH já cadastrada!");
    }
}
