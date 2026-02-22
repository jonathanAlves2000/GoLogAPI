package GoLogAPI.validation;

import GoLogAPI.dto.tractor.TractorCreateRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.TractorRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TractorValidator {

    TractorRepository tractorRepository;

    public  TractorValidator(TractorRepository tractorRepository){
        this.tractorRepository = tractorRepository;
    }

    List<String> errors = new ArrayList<>();

    public void validate(TractorCreateRequest tractorCreateRequest){

        plate(tractorCreateRequest.plate(), errors);
        renavam(tractorCreateRequest.renavam(), errors);

        if(!errors.isEmpty()){
            throw new ConflictException(errors);
        }
    }

    public void plate(String plate, List<String> errors){
        boolean exists = tractorRepository.existsByPlate(plate);
        if(exists){
            errors.add("Placa de tração já cadastrada!");
        }
    }

    public void renavam(String renavam, List<String> errors){
        boolean exists = tractorRepository.existsByRenavam(renavam);
        if(exists){
            errors.add("Renavam já cadastrado!");
        }
    }

}
