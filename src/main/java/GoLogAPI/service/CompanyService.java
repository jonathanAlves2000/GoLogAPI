package GoLogAPI.service;

import GoLogAPI.dto.CompanyDto;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.CompanyMapper;
import GoLogAPI.model.Address;
import GoLogAPI.model.Company;
import GoLogAPI.repository.AddressRepository;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.validation.CompanyValidator;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    private CompanyRepository companyRepository;
    private AddressRepository addressRepository;
    private CompanyMapper companyMapper;
    private CompanyValidator companyValidator;

    public CompanyService(CompanyRepository companyRepository, AddressRepository addressRepository, CompanyMapper companyMapper, CompanyValidator companyValidator){
        this.companyRepository = companyRepository;
        this.addressRepository = addressRepository;
        this.companyMapper = companyMapper;
        this.companyValidator = companyValidator;
    }

    public CompanyDto saveCompany(CompanyDto companyDto){
        companyValidator.companyValidate(companyDto);
        Company company = companyMapper.toEntity(companyDto);
        Address address = addressRepository.findById(companyDto.addressId())
                        .orElseThrow(() -> new  ResourceNotFoundException("Registro não encontrado para o ID: " + companyDto.addressId()));
        company.setAddress(address);
        companyRepository.save(company);
        return companyMapper.toDto(company);
    }

    public CompanyDto getCompanyById(int id){
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para o ID: " + id));
        return companyMapper.toDto(company);
    }

    public void deleteCompanyById(int id){
        companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registro não encontrado para o ID: " + id));
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
