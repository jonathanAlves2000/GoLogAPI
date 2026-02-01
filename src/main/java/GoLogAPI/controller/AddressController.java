package GoLogAPI.controller;

import GoLogAPI.dto.address.AddressCreateRequest;
import GoLogAPI.dto.address.AddressPacthRequest;
import GoLogAPI.dto.address.AddressResponse;
import GoLogAPI.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService){
        this.addressService = addressService;
    }

    @PostMapping
    public AddressResponse SaveAddress(@Valid @RequestBody AddressCreateRequest addressCreateRequest){
        return addressService.saveAddress(addressCreateRequest);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<AddressResponse> GetAddress(@PathVariable UUID id){
        AddressResponse addressResponse = addressService.getAddress(id);
        return ResponseEntity.ok(addressResponse);
    }

    @DeleteMapping("{id}")
    public void DeleteAddress(@PathVariable UUID id){
        addressService.deleteAdrress(id);
    }

    @PutMapping("{id}")
    public AddressResponse PutAddress(@PathVariable UUID id, @Valid @RequestBody AddressCreateRequest addressCreateRequest){
        return addressService.putAddress(id, addressCreateRequest);
    }

    @PatchMapping("{id}")
    public AddressResponse PatchAddress(@PathVariable UUID id, @Valid @RequestBody AddressPacthRequest addressPacthRequest){
        return addressService.patchAddress(id, addressPacthRequest);
    }

}
