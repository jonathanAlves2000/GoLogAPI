package GoLogAPI.controller;

import GoLogAPI.dto.typeTransport.TypeTransportCreateRequest;
import GoLogAPI.dto.typeTransport.TypeTransportResponse;
import GoLogAPI.dto.typeTransport.TypeTransportUpdateRequest;
import GoLogAPI.service.TypeTransportService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/type-transport")
public class TypeTransportController {

    private final TypeTransportService typeTransportService;

    public TypeTransportController(TypeTransportService typeTransportService){
        this.typeTransportService = typeTransportService;
    }

    @PostMapping
    public ResponseEntity<TypeTransportResponse> save(@Valid @RequestBody TypeTransportCreateRequest typeTransportCreateRequest){
        TypeTransportResponse typeTransportResponse = typeTransportService.save(typeTransportCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(typeTransportResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(typeTransportResponse);
    }

    @RequestMapping(value = "{id}", method ={RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<TypeTransportResponse> get(@PathVariable("id") UUID id){
        TypeTransportResponse typeTransportResponse = typeTransportService.get(id);
        return ResponseEntity.ok(typeTransportResponse);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        typeTransportService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<TypeTransportResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody TypeTransportCreateRequest typeTransportCreateRequest){
        TypeTransportResponse typeTransportResponse = typeTransportService.update(id, typeTransportCreateRequest);
        return ResponseEntity.ok(typeTransportResponse);
    }

    @PatchMapping("{id}")
    public ResponseEntity<TypeTransportResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody TypeTransportUpdateRequest TypeTransportUpdateRequest){
        TypeTransportResponse typeTransportResponse = typeTransportService.updatePartial(id, TypeTransportUpdateRequest);
        return ResponseEntity.ok(typeTransportResponse);
    }
}
