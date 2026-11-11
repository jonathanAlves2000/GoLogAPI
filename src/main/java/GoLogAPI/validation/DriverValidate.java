package GoLogAPI.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import GoLogAPI.dto.driver.DriverRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.DriverRepository;

@Component
public class DriverValidate {

    private final DriverRepository driverRepository;

    public DriverValidate(DriverRepository driverRepository){
        this.driverRepository = driverRepository;
    }

    public void validate(DriverRequest driverRequest){
        validate(driverRequest, null);
    }

    /** @param selfId id do próprio motorista em edições (não conflita com a própria CNH). */
    public void validate(DriverRequest driverRequest, java.util.UUID selfId){
        List<String> errors = new ArrayList<>();

        if (driverRequest.cnhNumber() != null && !driverRequest.cnhNumber().isBlank())
            cnh(driverRequest.cnhNumber(), selfId, errors);

        if(!errors.isEmpty()) throw new ConflictException(errors);
    }

    public void cnh(String cnh, java.util.UUID selfId, List<String> errors){
        boolean exists = driverRepository.findAll().stream()
                .anyMatch(d -> cnh.equals(d.getCnhNumber()) && !d.getId().equals(selfId));
        if(exists) errors.add("CNH já cadastrada!");
    }
}
