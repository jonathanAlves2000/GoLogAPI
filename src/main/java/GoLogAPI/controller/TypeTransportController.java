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
@Tag(name = "Tipo de Transporte")
public class TypeTransportController {

    private final TypeTransportService typeTransportService;

    public TypeTransportController(TypeTransportService typeTransportService){
        this.typeTransportService = typeTransportService;
    }

    @Operation(summary = "Cadastrar Tipo de Transporte", description = "Cadastra um novo tipo de transporte no sistema")
    @PostMapping
    public ResponseEntity<TypeTransportResponse> save(@Valid @RequestBody TypeTransportCreateRequest typeTransportCreateRequest){
        TypeTransportResponse typeTransportResponse = typeTransportService.save(typeTransportCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(typeTransportResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(typeTransportResponse);
    }

    @Operation(summary = "Listar Tipos de Transporte", description = "Retorna uma lista de todos os tipos de transporte cadastrados")
    @GetMapping
    public ResponseEntity<List<TypeTransportResponse>> getAll() {
        List<TypeTransportResponse> typeTransports = typeTransportService.getAll();
        return ResponseEntity.ok(typeTransports);
    }

    @Operation(summary = "Exibir Tipo de Transporte", description = "Exibe os detalhes de um tipo de transporte específico pelo ID")
    @RequestMapping(value = "{id}", method ={RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<TypeTransportResponse> get(@PathVariable("id") UUID id){
        TypeTransportResponse typeTransportResponse = typeTransportService.get(id);
        return ResponseEntity.ok(typeTransportResponse);
    }

    @Operation(summary = "Excluir Tipo de Transporte", description = "Exclui um tipo de transporte específico pelo ID")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        typeTransportService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar Tipo de Transporte", description = "Atualiza todos os dados de um tipo de transporte existente")
    @PutMapping("{id}")
    public ResponseEntity<TypeTransportResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody TypeTransportCreateRequest typeTransportCreateRequest){
        TypeTransportResponse typeTransportResponse = typeTransportService.update(id, typeTransportCreateRequest);
        return ResponseEntity.ok(typeTransportResponse);
    }

    @Operation(summary = "Atualizar Tipo de Transporte Parcialmente", description = "Atualiza parcialmente os dados de um tipo de transporte existente")
    @PatchMapping("{id}")
    public ResponseEntity<TypeTransportResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody TypeTransportUpdateRequest TypeTransportUpdateRequest){
        TypeTransportResponse typeTransportResponse = typeTransportService.updatePartial(id, TypeTransportUpdateRequest);
        return ResponseEntity.ok(typeTransportResponse);
    }
}
