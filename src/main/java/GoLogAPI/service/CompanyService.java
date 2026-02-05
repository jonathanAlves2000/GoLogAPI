package GoLogAPI.service;

import GoLogAPI.dto.company.CompanyCreateRequest;
import GoLogAPI.dto.company.CompanyPatchRequest;
import GoLogAPI.dto.company.CompanyResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.CompanyMapper;
import GoLogAPI.model.Address;
import GoLogAPI.model.Company;
import GoLogAPI.repository.AddressRepository;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.validation.CompanyValidator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class CompanyService {

    private CompanyRepository companyRepository;
    private AddressRepository addressRepository;
    private CompanyMapper companyMapper;
    private CompanyValidator companyValidator;

    String message = "Registro não encontrado para o Id: ";

    public CompanyService(CompanyRepository companyRepository, AddressRepository addressRepository, CompanyMapper companyMapper, CompanyValidator companyValidator){
        this.companyRepository = companyRepository;
        this.addressRepository = addressRepository;
        this.companyMapper = companyMapper;
        this.companyValidator = companyValidator;
    }

    public CompanyResponse saveCompany(CompanyCreateRequest companyCreateRequest){
        companyValidator.companyValidate(companyCreateRequest);
        Company company = companyMapper.toEntity(companyCreateRequest);
        Address address = addressRepository.findById(companyCreateRequest.addressId())
                        .orElseThrow(() -> new  ResourceNotFoundException(message + companyCreateRequest.addressId()));
        company.setAddress(address);
        companyRepository.save(company);
        return companyMapper.toResponse(company);
    }

    public CompanyResponse getCompany(UUID id){
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));
        return companyMapper.toResponse(company);
    }

    public void deleteCompany(UUID id){
        companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));
        companyRepository.deleteById(id);
    }

    public CompanyResponse putCompany(UUID id, CompanyCreateRequest companyCreateRequest){
        companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));
        Address address = addressRepository.findById(companyCreateRequest.addressId())
                .orElseThrow(() -> new ResourceNotFoundException(message + companyCreateRequest.addressId()));
        Company company = companyMapper.toEntity(companyCreateRequest);
        company.setAddress(address);
        company.setId(id);
        companyRepository.save(company);
        return companyMapper.toResponse(company);
    }

    @Transactional
    public CompanyResponse patchCompany(UUID id, CompanyPatchRequest companyPatchRequest){
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));

        if(companyPatchRequest.cnpjCpf() != null) company.setCnpjCpf(companyPatchRequest.cnpjCpf());
        if(companyPatchRequest.isCliente() != null) company.setIsCliente(companyPatchRequest.isCliente());
        if(companyPatchRequest.email() != null) company.setEmail(companyPatchRequest.email());
        if(companyPatchRequest.legalName() != null) company.setLegalName(companyPatchRequest.legalName());
        if(companyPatchRequest.phoneNumber() != null) company.setPhoneNumber(companyPatchRequest.phoneNumber());
        if(companyPatchRequest.addressId() != null){
            Address address = addressRepository.findById(companyPatchRequest.addressId())
                    .orElseThrow(() -> new ResourceNotFoundException(message + companyPatchRequest.addressId()));
            company.setAddress(address);
        }
        companyRepository.save(company);
        return companyMapper.toResponse(company);
    }
}
