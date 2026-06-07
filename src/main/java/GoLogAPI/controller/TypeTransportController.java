package GoLogAPI.controller;

import GoLogAPI.dto.typeTransport.TypeTransportCreateRequest;
import GoLogAPI.dto.typeTransport.TypeTransportResponse;
import GoLogAPI.dto.typeTransport.TypeTransportUpdateRequest;
import GoLogAPI.service.TypeTransportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/type-transport")
@Tag(name = "Type Transport")
public class TypeTransportController {

    private final TypeTransportService typeTransportService;

    public TypeTransportController(TypeTransportService typeTransportService){
        this.typeTransportService = typeTransportService;
    }

    @Operation(summary = "Create", description = "Create Type Transport")
    @PostMapping
    public ResponseEntity<TypeTransportResponse> save(@Valid @RequestBody TypeTransportCreateRequest typeTransportCreateRequest){
        TypeTransportResponse typeTransportResponse = typeTransportService.save(typeTransportCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(typeTransportResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(typeTransportResponse);
    }

    @Operation(summary = "Display List", description = "Display Type Transport List")
    @GetMapping
    public ResponseEntity<List<TypeTransportResponse>> getAll() {
        List<TypeTransportResponse> typeTransports = typeTransportService.getAll();
        return ResponseEntity.ok(typeTransports);
    }

    @Operation(summary = "Display", description = "Display Type Transport")
    @RequestMapping(value = "{id}", method ={RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<TypeTransportResponse> get(@PathVariable("id") UUID id){
        TypeTransportResponse typeTransportResponse = typeTransportService.get(id);
        return ResponseEntity.ok(typeTransportResponse);
    }

    @Operation(summary = "Delete", description = "Delete Type Transport")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        typeTransportService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update", description = "Update Type Transport")
    @PutMapping("{id}")
    public ResponseEntity<TypeTransportResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody TypeTransportCreateRequest typeTransportCreateRequest){
        TypeTransportResponse typeTransportResponse = typeTransportService.update(id, typeTransportCreateRequest);
        return ResponseEntity.ok(typeTransportResponse);
    }

    @Operation(summary = "Update Partial", description = "Update Partial Type Transport")
    @PatchMapping("{id}")
    public ResponseEntity<TypeTransportResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody TypeTransportUpdateRequest TypeTransportUpdateRequest){
        TypeTransportResponse typeTransportResponse = typeTransportService.updatePartial(id, TypeTransportUpdateRequest);
        return ResponseEntity.ok(typeTransportResponse);
    }
}
