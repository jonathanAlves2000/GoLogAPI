package GoLogAPI.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import GoLogAPI.dto.company.CompanyRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.CompanyRepository;

@Component
public class CompanyValidator {

    private final CompanyRepository companyRepository;

    public CompanyValidator(CompanyRepository companyRepository){
        this.companyRepository = companyRepository;
    }

    public void validate(CompanyRequest companyRequest){
        List<String> errors = new ArrayList<>();
        if(companyRequest.legalName() != null && !companyRequest.legalName().isBlank())
            legalName(companyRequest.legalName(), errors);

        if(companyRequest.phoneNumber() != null && !companyRequest.phoneNumber().isBlank())
            phoneNumber(companyRequest.phoneNumber(), errors);

        if(companyRequest.email() != null && !companyRequest.email().isBlank())
            email(companyRequest.email(), errors);

        if(companyRequest.cnpjCpf() != null && !companyRequest.cnpjCpf().isBlank())
            cnpjCpf(companyRequest.cnpjCpf(), errors);

        if(!errors.isEmpty())
            throw new ConflictException(errors);
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
