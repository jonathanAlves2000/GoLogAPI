package GoLogAPI.service;

import GoLogAPI.dto.address.AddressCreateRequest;
import GoLogAPI.dto.address.AddressUpdateRequest;
import GoLogAPI.dto.address.AddressResponse;
import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.mapper.AddressMapper;
import GoLogAPI.model.Address;
import GoLogAPI.repository.AddressRepository;
import GoLogAPI.validation.AddressValidator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AddressService {

    private AddressRepository addressRepository;
    private AddressMapper addressMapper;
    private AddressValidator addressValidator;

    public AddressService(AddressRepository addressRepository, AddressMapper addressMapper, AddressValidator addressValidator){
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
        this.addressValidator = addressValidator;
    }

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

    public List<AddressResponse> listAll(){
        List<Address> adddressList = addressRepository.findAll();
        return addressMapper.toResponses(adddressList);
    }

    public void delete(UUID id){
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(MessageException.NOT_FOUND_MESSAGE, id));
        addressRepository.delete(address);
    }

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

        if(addressUpdateRequest.cep() != null) address.setCep(addressUpdateRequest.cep());
        if(addressUpdateRequest.city() != null) address.setCity(addressUpdateRequest.city());
        if(addressUpdateRequest.country() != null) address.setCountry(addressUpdateRequest.country());
        if(addressUpdateRequest.district() != null) address.setDistrict(addressUpdateRequest.district());
        if(addressUpdateRequest.number() != null) address.setNumber(addressUpdateRequest.number());
        if(addressUpdateRequest.state() != null) address.setState(addressUpdateRequest.state());
        if(addressUpdateRequest.street() != null) address.setStreet(addressUpdateRequest.street());
        if(addressUpdateRequest.complement() != null) address.setComplement(addressUpdateRequest.complement());

        addressRepository.save(address);
        return addressMapper.toResponse(address);
    }

}
