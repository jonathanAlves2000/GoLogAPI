package GoLogAPI.service;

import GoLogAPI.dto.address.AddressCreateRequest;
import GoLogAPI.dto.address.AddressUpdateRequest;
import GoLogAPI.dto.address.AddressResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.infra.tenant.TenantContext;
import GoLogAPI.mapper.AddressMapper;
import GoLogAPI.model.Address;
import GoLogAPI.model.Company;
import GoLogAPI.repository.AddressRepository;
import GoLogAPI.repository.CompanyRepository;
import GoLogAPI.validation.AddressValidate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final AddressValidate addressValidate;
    private final CompanyRepository companyRepository;
    private final GoLogAPI.infra.tenant.TenantGuard tenantGuard;

    public AddressService(AddressRepository addressRepository, AddressMapper addressMapper,
                          AddressValidate addressValidate, CompanyRepository companyRepository,
                          GoLogAPI.infra.tenant.TenantGuard tenantGuard){
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
        this.addressValidate = addressValidate;
        this.companyRepository = companyRepository;
        this.tenantGuard = tenantGuard;
    }

    /** Endereço visível no escopo do tenant ativo (404 caso contrário). */
    public Address findVisible(UUID id) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Endereço não encontrado: ", id));
        tenantGuard.checkAccess(address.getCompany(), id);
        return address;
    }

    @Transactional
    public AddressResponse save(AddressCreateRequest addressCreateRequest){
        Address address = addressMapper.toEntity(addressCreateRequest);
        address.setCompany(tenantGuard.ownerForWrite(null));
        addressRepository.save(address);
        return addressMapper.toResponse(address);
    }

    public AddressResponse get(UUID id){
        Address address = findVisible(id);
        return addressMapper.toResponse(address);
    }

    public List<AddressResponse> getAll(){
        List<Address> addresses = tenantGuard.isGlobalView()
                ? addressRepository.findAll()
                : addressRepository.findByCompanyId(tenantGuard.scope());
        return addressMapper.toResponses(addresses);
    }

    @Transactional
    public void delete(UUID id){
        Address address = findVisible(id);
        addressRepository.delete(address);
    }

    @Transactional
    public AddressResponse update(UUID id, AddressCreateRequest addressCreateRequest){
        Address address = findVisible(id);
        address.setStreet(addressCreateRequest.street());
        address.setNumber(addressCreateRequest.number());
        address.setDistrict(addressCreateRequest.district());
        address.setCity(addressCreateRequest.city());
        address.setState(addressCreateRequest.state());
        address.setCountry(addressCreateRequest.country());
        address.setCep(addressCreateRequest.cep());
        address.setComplement(addressCreateRequest.complement());
        address.setLatitude(addressCreateRequest.latitude());
        address.setLongitude(addressCreateRequest.longitude());
        addressRepository.save(address);
        return addressMapper.toResponse(address);
    }

    @Transactional
    public AddressResponse updatePartial(UUID id, AddressUpdateRequest addressUpdateRequest){
        Address address = findVisible(id);

        if(addressUpdateRequest.cep() != null && !addressUpdateRequest.cep().isBlank())
            address.setCep(addressUpdateRequest.cep());
        if(addressUpdateRequest.city() != null && !addressUpdateRequest.city().isBlank())
            address.setCity(addressUpdateRequest.city());
        if(addressUpdateRequest.country() != null && !addressUpdateRequest.country().isBlank())
            address.setCountry(addressUpdateRequest.country());
        if(addressUpdateRequest.district() != null && !addressUpdateRequest.district().isBlank())
            address.setDistrict(addressUpdateRequest.district());
        if(addressUpdateRequest.number() != null && !addressUpdateRequest.number().isBlank())
            address.setNumber(addressUpdateRequest.number());
        if(addressUpdateRequest.state() != null && !addressUpdateRequest.state().isBlank())
            address.setState(addressUpdateRequest.state());
        if(addressUpdateRequest.street() != null && !addressUpdateRequest.street().isBlank())
            address.setStreet(addressUpdateRequest.street());
        if(addressUpdateRequest.complement() != null && !addressUpdateRequest.complement().isBlank())
            address.setComplement(addressUpdateRequest.complement());
        if(addressUpdateRequest.latitude() != null && !addressUpdateRequest.latitude().isBlank())
            address.setLatitude(addressUpdateRequest.latitude());
        if(addressUpdateRequest.longitude() != null && !addressUpdateRequest.longitude().isBlank())
            address.setLongitude(addressUpdateRequest.longitude());

        addressRepository.save(address);
        return addressMapper.toResponse(address);
    }

}
