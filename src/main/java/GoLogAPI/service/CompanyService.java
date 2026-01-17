package GoLogAPI.service;

import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.Company;
import GoLogAPI.repository.CompanyRepository;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    private CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository){
        this.companyRepository = companyRepository;
    }

    public Company saveCompany(Company company){
        return this.companyRepository.save(company);
    }

    public Company getCompanyById(int id){
        if(!companyRepository.existsById(id)){
            throw new ResourceNotFoundException("Registro não encontrado para o ID: " + id);
        }
        return companyRepository.findById(id).orElse(null);
    }

    public void deleteCompanyById(int id){
        if(!companyRepository.existsById(id)){
            throw new ResourceNotFoundException("Registro não encontrado para o ID: " + id);
        }
         this.companyRepository.deleteById(id);
    }

    public Company updateCompanyById(int id, Company company){
        if(!companyRepository.existsById(id)){
            throw new ResourceNotFoundException("Registro não encontrado para o ID: " + id);
        }
        company.setId(id);
        return companyRepository.save(company);
    }
}
