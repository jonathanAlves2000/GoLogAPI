package GoLogAPI.controller;

import GoLogAPI.model.Address;
import GoLogAPI.service.AddressService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService){
        this.addressService = addressService;
    }

    @PostMapping
    public Address SaveAddress(@RequestBody Address address){
        return this.addressService.saveAddress(address);
    }

    @GetMapping("{id}")
    public Address GetAddressById(@PathVariable int id){
        return this.addressService.getAddressById(id);
    }

    @DeleteMapping("{id}")
    public void DeleteAddresById(@PathVariable int id){
        this.addressService.deleteAdrressById(id);
    }

    @PutMapping("{id}")
    public Address UpdateAddresById(@PathVariable int id, @RequestBody Address address){
        return this.addressService.updateAddress(id, address);
    }

}
