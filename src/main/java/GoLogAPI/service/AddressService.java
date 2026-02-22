package GoLogAPI.service;

import GoLogAPI.dto.address.AddressCreateRequest;
import GoLogAPI.dto.address.AddressPacthRequest;
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

    String message = "Registro não encontrado para o Id: ";

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
                .orElseThrow(() -> new ResourceNotFoundException(message + id));
        return addressMapper.toResponse(address);
    }

    public List<AddressResponse> listAll(){
        List<Address> adddressList = addressRepository.findAll();
        return addressMapper.toResponses(adddressList);
    }

    public void delete(UUID id){
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));
        addressRepository.delete(address);
    }

    public AddressResponse update(UUID id, AddressCreateRequest addressCreateRequest){
        addressValidator.validate(addressCreateRequest);
        addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));
        Address address = addressMapper.toEntity(addressCreateRequest);
        address.setId(id);
        addressRepository.save(address);
        return addressMapper.toResponse(address);
    }

    @Transactional
    public AddressResponse updatePartial(UUID id, AddressPacthRequest addressPacthRequest){
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(message + id));

        if(addressPacthRequest.cep() != null) address.setCep(addressPacthRequest.cep());
        if(addressPacthRequest.city() != null) address.setCity(addressPacthRequest.city());
        if(addressPacthRequest.country() != null) address.setCountry(addressPacthRequest.country());
        if(addressPacthRequest.district() != null) address.setDistrict(addressPacthRequest.district());
        if(addressPacthRequest.number() != null) address.setNumber(addressPacthRequest.number());
        if(addressPacthRequest.state() != null) address.setState(addressPacthRequest.state());
        if(addressPacthRequest.street() != null) address.setStreet(addressPacthRequest.street());
        if(addressPacthRequest.complement() != null) address.setComplement(addressPacthRequest.complement());

        addressRepository.save(address);
        return addressMapper.toResponse(address);
    }

}
