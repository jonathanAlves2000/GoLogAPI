package GoLogAPI.validation;

import GoLogAPI.dto.CompanyDto;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.CompanyRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CompanyValidator {

    private CompanyRepository companyRepository;

    public CompanyValidator(CompanyRepository companyRepository){
        this.companyRepository = companyRepository;
    }

    List<String> errorList = new ArrayList<>();

    public void companyValidate(CompanyDto companyDto){
        legalNameValidate(companyDto.legalName(), errorList);
        phoneNumberValidate(companyDto.phoneNumber(), errorList);

        if(!errorList.isEmpty()){
            throw new ConflictException(errorList);
        }
    }

    public void legalNameValidate(String legalName, List<String> errorList){
        boolean exists = companyRepository.existsByLegalName(legalName);
        if(exists){
            errorList.add("Nome Legal já registrado!");
        }
    }

    public void phoneNumberValidate(String phoneNumber, List<String> errorList){
        boolean exists = companyRepository.existsByPhoneNumber(phoneNumber);
        if(exists){
            errorList.add("Numero de Telefone já registrado!");
        }
    }



}
