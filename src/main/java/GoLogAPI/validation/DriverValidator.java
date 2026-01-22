package GoLogAPI.validation;

import GoLogAPI.dto.DriverDto;
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

    List<String> errorList = new ArrayList<>();

    public void driverValidate(DriverDto driverDto){
        cnhValidate(driverDto.cnhNumber(), errorList);

        if(!errorList.isEmpty()){
            throw new ConflictException(errorList);
        }
    }

    public void cnhValidate(String cnh, List<String> errorList){
        boolean exists = driverRepository.existsByCnhNumber(cnh);
        if(exists){
            errorList.add("CNH já cadastrada!");
        }
    }
}
