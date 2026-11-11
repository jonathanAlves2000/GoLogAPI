package GoLogAPI.validation;

import GoLogAPI.dto.trailer.TrailerRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.EquipamentRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Placa e RENAVAM são únicos em toda a frota (cavalos e carretas), ignorando o próprio registro em edições. */
@Component
public class TrailerValidate {

    private final EquipamentRepository equipamentRepository;

    public TrailerValidate(EquipamentRepository equipamentRepository){
        this.equipamentRepository = equipamentRepository;
    }

    public void validate(TrailerRequest request){
        validate(request, null);
    }

    public void validate(TrailerRequest request, UUID selfId){
        List<String> errors = new ArrayList<>();
        if(request.plate() != null && !request.plate().isBlank()
                && equipamentRepository.findAllByPlate(request.plate()).stream().anyMatch(e -> !e.getId().equals(selfId)))
            errors.add("Placa já cadastrada na frota!");
        if(request.renavam() != null && !request.renavam().isBlank()
                && equipamentRepository.findAllByRenavam(request.renavam()).stream().anyMatch(e -> !e.getId().equals(selfId)))
            errors.add("RENAVAM já cadastrado na frota!");
        if(!errors.isEmpty()) throw new ConflictException(errors);
    }
}
