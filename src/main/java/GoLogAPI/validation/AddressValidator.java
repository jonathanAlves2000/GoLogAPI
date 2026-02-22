package GoLogAPI.validation;

import GoLogAPI.dto.address.AddressCreateRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.AddressRepository;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class AddressValidator {

    private AddressRepository addressRepository;

    public AddressValidator(AddressRepository addressRepository){
        this.addressRepository = addressRepository;
    }

    List<String> erros = new ArrayList<>();

    public void validate(AddressCreateRequest addressCreateRequest){
        cepNumber(addressCreateRequest.cep(), addressCreateRequest.number(), erros);
        if(!erros.isEmpty()){
            throw new ConflictException(erros);
        }
    }

    public void cepNumber(String cep, String number, List<String> erros){
        boolean exists = addressRepository.existsByCepAndNumber(cep, number);

        if(exists){
            erros.add("Empresa com o mesmo cep e numero já registrados");
        }
    }

}
