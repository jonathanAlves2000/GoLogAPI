package GoLogAPI.validation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import GoLogAPI.dto.company.CompanyRequest;
import GoLogAPI.exception.ConflictException;
import GoLogAPI.repository.CompanyRepository;

@Component
public class CompanyValidate {

    private final CompanyRepository companyRepository;

    public CompanyValidate(CompanyRepository companyRepository){
        this.companyRepository = companyRepository;
    }

    public void validate(CompanyRequest companyRequest){
        List<String> errors = new ArrayList<>();

        if(companyRequest.cnpjCpf() != null && !companyRequest.cnpjCpf().isBlank())
            cnpjCpf(companyRequest.cnpjCpf(), errors);

        if(!errors.isEmpty())
            throw new ConflictException(errors);
    }

    public void cnpjCpf(String cnpjCpf, List<String> errors){
        String clean = cnpjCpf.replaceAll("\\D", "");
        boolean exists = companyRepository.existsByCnpjCpf(cnpjCpf)
                || (!clean.isBlank() && companyRepository.existsByCnpjCpf(clean));
        if(exists) errors.add("CNPJ ou CPF já registrado!");
    }
}
