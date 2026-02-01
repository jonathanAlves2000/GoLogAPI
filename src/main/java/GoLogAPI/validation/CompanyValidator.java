package GoLogAPI.validation;

import GoLogAPI.dto.company.CompanyCreateRequest;
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

    List<String> errors = new ArrayList<>();

    public void companyValidate(CompanyCreateRequest companyCreateRequest){
        legalNameValidate(companyCreateRequest.legalName(), errors);
        phoneNumberValidate(companyCreateRequest.phoneNumber(), errors);
        emailValidate(companyCreateRequest.email(), errors);
        cnpjCpfValidate(companyCreateRequest.cnpjCpf(), errors);

        if(!errors.isEmpty()){
            throw new ConflictException(errors);
        }
    }

    public void legalNameValidate(String legalName, List<String> errors){
        boolean exists = companyRepository.existsByLegalName(legalName);
        if(exists){
            errors.add("Nome Legal já registrado!");
        }
    }

    public void phoneNumberValidate(String phoneNumber, List<String> errors){
        boolean exists = companyRepository.existsByPhoneNumber(phoneNumber);
        if(exists){
            errors.add("Numero de Telefone já registrado!");
        }
    }

    public void emailValidate(String email, List<String> errors){
        boolean exists = companyRepository.existsByEmail(email);
        if(exists){
            errors.add("Email já registrado!");
        }
    }

    public void cnpjCpfValidate(String cnpjCpf, List<String> errors){
        boolean exists = companyRepository.existsByCnpjCpf(cnpjCpf);
        if(exists){
            errors.add("CNPJ ou CPF já registrado!");
        }
    }


}
