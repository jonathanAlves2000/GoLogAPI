package GoLogAPI.validation;

import GoLogAPI.dto.driver.DriverCreateRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.DriverRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DriverValidator {

    private DriverRepository driverRepository;

    public DriverValidator(DriverRepository driverRepository){
        this.driverRepository = driverRepository;
    }

    List<String> errors = new ArrayList<>();

    public void validate(DriverCreateRequest driverCreateRequest){
        cnh(driverCreateRequest.cnhNumber(), errors);

        if(!errors.isEmpty()){
            throw new ConflictException(errors);
        }
    }

    public void cnh(String cnh, List<String> errors){
        boolean exists = driverRepository.existsByCnhNumber(cnh);
        if(exists){
            errors.add("CNH já cadastrada!");
        }
    }
}
