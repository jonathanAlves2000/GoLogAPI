package GoLogAPI.validation;

import GoLogAPI.dto.address.AddressRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.AddressRepository;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class AddressValidator {

    private final AddressRepository addressRepository;

    public AddressValidator(AddressRepository addressRepository){
        this.addressRepository = addressRepository;
    }

    public void validate(AddressRequest addressRequest){
        List<String> erros = new ArrayList<>();
        cepNumber(addressRequest.cep(), addressRequest.number(), erros);
        if(!erros.isEmpty()){
            throw new ConflictException(erros);
        }
    }

    public void cepNumber(String cep, String number, List<String> erros){
        boolean exists = addressRepository.existsByCepAndNumber(cep, number);
        if(exists) erros.add("Empresa com o mesmo cep e numero já registrados");
    }

}
