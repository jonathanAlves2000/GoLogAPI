package GoLogAPI.service;

import GoLogAPI.dto.company.*;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.CompanyMapper;
import GoLogAPI.model.Address;
import GoLogAPI.model.Company;
import org.springframework.transaction.annotation.Transactional;
import GoLogAPI.repository.AddressRepository;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.validation.CompanyValidator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final AddressRepository addressRepository;
    private final CompanyMapper companyMapper;
    private final CompanyValidator companyValidator;

    public CompanyService(CompanyRepository companyRepository, AddressRepository addressRepository, CompanyMapper companyMapper, CompanyValidator companyValidator){
        this.companyRepository = companyRepository;
        this.addressRepository = addressRepository;
        this.companyMapper = companyMapper;
        this.companyValidator = companyValidator;
    }

    @Transactional
    public CompanyResponse save(CompanyCreateRequest companyCreateRequest){
        companyValidator.validate(companyCreateRequest);
        Company company = companyMapper.toEntity(companyCreateRequest);
        Address address = addressRepository.findById(companyCreateRequest.addressId())
                        .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, companyCreateRequest.addressId()));
        company.setAddress(address);
        companyRepository.save(company);
        return companyMapper.toResponse(company);
    }

    public CompanyResponse get(UUID id){
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return companyMapper.toResponse(company);
    }

    public List<CompanyResponseList> listAll(){
        List<Company> companies = companyRepository.findAll();
        return companyMapper.toResponses(companies);
    }

    @Transactional
    public void delete(UUID id){
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        companyRepository.delete(company);
    }

    @Transactional
    public CompanyResponse update(UUID id, CompanyCreateRequest companyCreateRequest){
        companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        Address address = addressRepository.findById(companyCreateRequest.addressId())
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, companyCreateRequest.addressId()));
        Company company = companyMapper.toEntity(companyCreateRequest);
        company.setAddress(address);
        company.setId(id);
        companyRepository.save(company);
        return companyMapper.toResponse(company);
    }

    @Transactional
    public CompanyResponse updatePartial(UUID id, CompanyUpdateRequest companyUpdateRequest){
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        companyValidator.validate(companyUpdateRequest);

        if(companyUpdateRequest.cnpjCpf() != null) company.setCnpjCpf(companyUpdateRequest.cnpjCpf());
        if(companyUpdateRequest.isCliente() != null) company.setIsCliente(companyUpdateRequest.isCliente());
        if(companyUpdateRequest.email() != null) company.setEmail(companyUpdateRequest.email());
        if(companyUpdateRequest.legalName() != null) company.setLegalName(companyUpdateRequest.legalName());
        if(companyUpdateRequest.phoneNumber() != null) company.setPhoneNumber(companyUpdateRequest.phoneNumber());
        if(companyUpdateRequest.addressId() != null){
            Address address = addressRepository.findById(companyUpdateRequest.addressId())
                    .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, companyUpdateRequest.addressId()));
            company.setAddress(address);
        }
        companyRepository.save(company);
        return companyMapper.toResponse(company);
    }
}
