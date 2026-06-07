package GoLogAPI.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import GoLogAPI.dto.address.AddressCreateRequest;
import GoLogAPI.dto.address.AddressResponse;
import GoLogAPI.dto.address.AddressUpdateRequest;
import GoLogAPI.service.AddressService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/address")
@Tag(name = "Endereço")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService){
        this.addressService = addressService;
    }

    @Operation(summary = "Cadastrar Endereço", description = "Cadastra um novo endereço no sistema")
    @PostMapping
    public ResponseEntity<AddressResponse> save(@Valid @RequestBody AddressCreateRequest addressCreateRequest){
        AddressResponse addressResponse = addressService.save(addressCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(addressResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(addressResponse);
    }

    @Operation(summary = "Exibir Endereço", description = "Exibe os detalhes de um endereço específico pelo ID")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<AddressResponse> get(@PathVariable("id") UUID id){
        AddressResponse addressResponse = addressService.get(id);
        return ResponseEntity.ok(addressResponse);
    }

    @GetMapping
    @Operation(summary = "Listar Endereços", description = "Retorna uma lista de todos os endereços cadastrados")
    public ResponseEntity<List<AddressResponse>> getAll(){
        List<AddressResponse> addressResponses = addressService.getAll();
        return ResponseEntity.ok(addressResponses);
    }

    @Operation(summary = "Excluir Endereço", description = "Exclui um endereço específico pelo ID")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar Endereço", description = "Atualiza todos os dados de um endereço existente")
    @PutMapping("{id}")
    public ResponseEntity<AddressResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody AddressCreateRequest addressCreateRequest){
        AddressResponse addressResponse = addressService.update(id, addressCreateRequest);
        return ResponseEntity.ok().body(addressResponse);
    }

    @Operation(summary = "Atualizar Endereço Parcialmente", description = "Atualiza parcialmente os dados de um endereço existente")
    @PatchMapping("{id}")
    public ResponseEntity<AddressResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody AddressUpdateRequest addressUpdateRequest){
        AddressResponse addressResponse = addressService.updatePartial(id, addressUpdateRequest);
        return ResponseEntity.ok().body(addressResponse);
    }
}
