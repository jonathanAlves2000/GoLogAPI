package GoLogAPI.service;

import GoLogAPI.dto.address.AddressCreateRequest;
import GoLogAPI.dto.address.AddressUpdateRequest;
import GoLogAPI.dto.address.AddressResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.AddressMapper;
import GoLogAPI.model.Address;
import GoLogAPI.repository.AddressRepository;
import GoLogAPI.validation.AddressValidator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final AddressValidator addressValidator;

    public AddressService(AddressRepository addressRepository, AddressMapper addressMapper, AddressValidator addressValidator){
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
        this.addressValidator = addressValidator;
    }

    @Transactional
    public AddressResponse save(AddressCreateRequest addressCreateRequest){
        addressValidator.validate(addressCreateRequest);
        Address address = addressMapper.toEntity(addressCreateRequest);
        addressRepository.save(address);
        return addressMapper.toResponse(address);
    }

    public AddressResponse get(UUID id){
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        return addressMapper.toResponse(address);
    }

    public List<AddressResponse> getAll(){
        List<Address> adddressList = addressRepository.findAll();
        return addressMapper.toResponses(adddressList);
    }

    @Transactional
    public void delete(UUID id){
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        addressRepository.delete(address);
    }

    @Transactional
    public AddressResponse update(UUID id, AddressCreateRequest addressCreateRequest){
        addressValidator.validate(addressCreateRequest);
        addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        Address address = addressMapper.toEntity(addressCreateRequest);
        address.setId(id);
        addressRepository.save(address);
        return addressMapper.toResponse(address);
    }

    @Transactional
    public AddressResponse updatePartial(UUID id, AddressUpdateRequest addressUpdateRequest){
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        addressValidator.validate(addressUpdateRequest);

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
