package GoLogAPI.service;

import GoLogAPI.exception.ResourceNotFoundException;
import GoLogAPI.model.Address;
import GoLogAPI.repository.AddressRepository;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

    private AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository){
        this.addressRepository = addressRepository;
    }

    public Address saveAddress(Address address){
        return addressRepository.save(address);
    }

    public Address getAddressById(int id){
        if(!addressRepository.existsById(id)){
            throw new ResourceNotFoundException("Endereço não encontrado para o ID: " + id);
        }
        return addressRepository.findById(id).orElse(null);
    }

    public void deleteAdrressById(int id){
        if(!addressRepository.existsById(id)){
            throw  new ResourceNotFoundException("Endereço não encontrado para o ID: " + id);
        }
        addressRepository.deleteById(id);
    }

    public Address updateAddress(int id, Address address){
        if(!addressRepository.existsById(id)){
            throw new ResourceNotFoundException("Endereço não encontrado para o ID: " + id);
        }
        address.setId(id);
        return addressRepository.save(address);
    }

}
