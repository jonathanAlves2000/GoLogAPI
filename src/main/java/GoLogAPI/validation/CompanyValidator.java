package GoLogAPI.validation;

import GoLogAPI.dto.company.CompanyCreateRequest;
import GoLogAPI.dto.company.CompanyRequest;
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


    public void validate(CompanyRequest companyRequest){
        List<String> errors = new ArrayList<>();
        legalName(companyRequest.legalName(), errors);
        phoneNumber(companyRequest.phoneNumber(), errors);
        email(companyRequest.email(), errors);
        cnpjCpf(companyRequest.cnpjCpf(), errors);

        if(!errors.isEmpty()){
            throw new ConflictException(errors);
        }
    }

    public void legalName(String legalName, List<String> errors){
        boolean exists = companyRepository.existsByLegalName(legalName);
        if(exists) errors.add("Nome Legal já registrado!");
    }

    public void phoneNumber(String phoneNumber, List<String> errors){
        boolean exists = companyRepository.existsByPhoneNumber(phoneNumber);
        if(exists) errors.add("Numero de Telefone já registrado!");
    }

    public void email(String email, List<String> errors){
        boolean exists = companyRepository.existsByEmail(email);
        if(exists) errors.add("Email já registrado!");
    }

    public void cnpjCpf(String cnpjCpf, List<String> errors){
        boolean exists = companyRepository.existsByCnpjCpf(cnpjCpf);
        if(exists) errors.add("CNPJ ou CPF já registrado!");
    }


}
