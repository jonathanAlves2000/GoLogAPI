package GoLogAPI.controller;

import GoLogAPI.dto.address.AddressCreateRequest;
import GoLogAPI.dto.address.AddressPacthRequest;
import GoLogAPI.dto.address.AddressResponse;
import GoLogAPI.dto.user.UserResponse;
import GoLogAPI.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/address")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService){
        this.addressService = addressService;
    }

    @PostMapping
    public AddressResponse save(@Valid @RequestBody AddressCreateRequest addressCreateRequest){
        return addressService.save(addressCreateRequest);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<AddressResponse> get(@PathVariable UUID id){
        AddressResponse addressResponse = addressService.get(id);
        return ResponseEntity.ok(addressResponse);
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> listAll(){
        List<AddressResponse> addressResponses = addressService.listAll();
        return ResponseEntity.ok(addressResponses);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable UUID id){
        addressService.delete(id);
    }

    @PutMapping("{id}")
    public AddressResponse update(@PathVariable UUID id, @Valid @RequestBody AddressCreateRequest addressCreateRequest){
        return addressService.update(id, addressCreateRequest);
    }

    @PatchMapping("{id}")
    public AddressResponse updatePartial(@PathVariable UUID id, @Valid @RequestBody AddressPacthRequest addressPacthRequest){
        return addressService.updatePartial(id, addressPacthRequest);
    }

}
