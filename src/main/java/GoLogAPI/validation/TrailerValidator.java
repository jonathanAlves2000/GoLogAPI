package GoLogAPI.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import GoLogAPI.dto.trailer.TrailerRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.TrailerRepository;

@Component
public class TrailerValidator {

    private final TrailerRepository trailerRepository;

    public TrailerValidator(TrailerRepository trailerRepository){
        this.trailerRepository = trailerRepository;
    }

    public void validate(TrailerRequest trailerRequest){
        List<String> errors = new ArrayList<>();
        plate(trailerRequest.plate(), errors);
        renavam(trailerRequest.renavam(), errors);
        if(!errors.isEmpty()) throw new ConflictException(errors);
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
