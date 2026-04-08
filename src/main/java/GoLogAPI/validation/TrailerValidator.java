package GoLogAPI.validation;

import GoLogAPI.dto.trailer.TrailerCreateRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.TrailerRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TrailerValidator {

    private TrailerRepository trailerRepository;

    public TrailerValidator(TrailerRepository trailerRepository){
        this.trailerRepository = trailerRepository;
    }

    List<String> errors = new ArrayList<>();

    public void validate(TrailerCreateRequest trailerCreateRequest){
        plate(trailerCreateRequest.plate(), errors);
        renavam(trailerCreateRequest.renavam(), errors);

        if(!errors.isEmpty()) {
            throw new ConflictException(errors);
        }
    }

    public void plate(String plate, List<String> errors){
        boolean exists = trailerRepository.existsByPlate(plate);
        if (exists) errors.add("Placa de reboque já cadastrada!");
    }

    public void renavam(String renavam, List<String> errors){
        boolean exists = trailerRepository.existsByRenavam(renavam);
        if(exists) errors.add("Renavam já cadastrado!");
    }

}
