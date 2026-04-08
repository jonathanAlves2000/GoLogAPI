package GoLogAPI.controller;

import GoLogAPI.dto.address.AddressCreateRequest;
import GoLogAPI.dto.address.AddressPacthRequest;
import GoLogAPI.dto.address.AddressResponse;
import GoLogAPI.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.net.URL;
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
    public ResponseEntity<AddressResponse> save(@Valid @RequestBody AddressCreateRequest addressCreateRequest){
        AddressResponse addressResponse = addressService.save(addressCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(addressResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(addressResponse);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<AddressResponse> get(@PathVariable("id") UUID id){
        AddressResponse addressResponse = addressService.get(id);
        return ResponseEntity.ok(addressResponse);
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> listAll(){
        List<AddressResponse> addressResponses = addressService.listAll();
        return ResponseEntity.ok(addressResponses);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<AddressResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody AddressCreateRequest addressCreateRequest){
        AddressResponse addressResponse = addressService.update(id, addressCreateRequest);
        return ResponseEntity.ok().body(addressResponse);
    }

    @PatchMapping("{id}")
    public ResponseEntity<AddressResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody AddressPacthRequest addressPacthRequest){
        AddressResponse addressResponse = addressService.updatePartial(id, addressPacthRequest);
        return ResponseEntity.ok().body(addressResponse);
    }

}
