package GoLogAPI.controller;

import GoLogAPI.dto.transport.TransportCreateRequest;
import GoLogAPI.dto.transport.TransportCreateResponse;
import GoLogAPI.dto.transport.TransportResponse;
import GoLogAPI.dto.transport.TransportUpdateRequest;
import GoLogAPI.service.TransportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transport")
@Tag(name = "Transporte")
public class TransportController {

    private final TransportService transportService;

    public TransportController(TransportService transportService) {
        this.transportService = transportService;
    }

    @Operation(summary = "Cadastrar Transporte", description = "Cadastra uma nova viagem/transporte no sistema")
    @PostMapping
    public ResponseEntity<TransportCreateResponse> save(@RequestBody @Valid TransportCreateRequest transportCreateRequest) {
        TransportCreateResponse transportCreateResponse = transportService.save(transportCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(transportCreateResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(transportCreateResponse);
    }

    @Operation(summary = "Exibir Transporte", description = "Exibe os detalhes de um transporte específico pelo ID")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<TransportResponse> get(@PathVariable("id") @NotNull(message = "Id não pode ser nulo.") UUID id){
        TransportResponse transportResponse = transportService.get(id);
        return ResponseEntity.ok(transportResponse);
    }

    @Operation(summary = "Excluir Transporte", description = "Exclui um transporte específico pelo ID")
    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        transportService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar Transportes", description = "Retorna uma lista de todos os transportes cadastrados")
    @GetMapping
    public ResponseEntity<List<TransportResponse>> getAll(){
        List<TransportResponse> transportResponses = transportService.getAll();
        return ResponseEntity.ok(transportResponses);
    }

    @Operation(summary = "Atualizar Transporte", description = "Atualiza todos os dados de um transporte existente")
    @PutMapping(value = "{id}")
    public ResponseEntity<TransportResponse> update(
            @PathVariable("id") UUID id,
            @RequestBody @Valid TransportCreateRequest transportCreateRequest){
        TransportResponse transportResponse = transportService.update(id, transportCreateRequest);
        return ResponseEntity.ok(transportResponse);
    }

    @Operation(summary = "Atualizar Transporte Parcialmente", description = "Atualiza parcialmente os dados de um transporte existente")
    @PatchMapping(value = "{id}")
    public ResponseEntity<TransportResponse> updatePartial(
            @PathVariable("id") UUID id,
            @Valid @RequestBody TransportUpdateRequest transportUpdateRequest){
        TransportResponse transportResponse = transportService.updatePartial(id, transportUpdateRequest);
        return ResponseEntity.ok(transportResponse);
    }

}
