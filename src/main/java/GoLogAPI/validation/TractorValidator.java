package GoLogAPI.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import GoLogAPI.dto.tractor.TractorRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.TractorRepository;

@Component
public class TractorValidator {

    TractorRepository tractorRepository;

    public  TractorValidator(TractorRepository tractorRepository){
        this.tractorRepository = tractorRepository;
    }

    public void validate(TractorRequest tractorRequest){
        List<String> errors = new ArrayList<>();
        plate(tractorRequest.plate(), errors);
        renavam(tractorRequest.renavam(), errors);

        if(!errors.isEmpty()){
            throw new ConflictException(errors);
        }
    }

    public void plate(String plate, List<String> errors){
        boolean exists = tractorRepository.existsByPlate(plate);
        if(exists) errors.add("Placa de tração já cadastrada!");
    }

    public void renavam(String renavam, List<String> errors){
        boolean exists = tractorRepository.existsByRenavam(renavam);
        if(exists) errors.add("Renavam já cadastrado!");
    }

}
