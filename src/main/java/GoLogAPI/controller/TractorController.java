package GoLogAPI.controller;

import GoLogAPI.dto.tractor.TractorCreateRequest;
import GoLogAPI.dto.tractor.TractorUpdateRequest;
import GoLogAPI.dto.tractor.TractorResponse;
import GoLogAPI.service.TractorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tractor")
@Tag(name = "Trator")
public class TractorController {

    private final TractorService tractorService;

    public TractorController(TractorService tractorService){
        this.tractorService = tractorService;
    }

    @Operation(summary = "Cadastrar Trator", description = "Cadastra um novo trator/cavalo mecânico no sistema")
    @PostMapping
    public ResponseEntity<TractorResponse> save(@Valid @RequestBody TractorCreateRequest tractorCreateRequest){
        TractorResponse tractorResponse = tractorService.save(tractorCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(tractorResponse.getId())
                .toUri();
        return ResponseEntity.status(HttpStatus.CREATED).body(tractorResponse);
    }

    @Operation(summary = "Listar Tratores", description = "Retorna uma lista de todos os tratores cadastrados")
    @GetMapping
    public ResponseEntity<List<TractorResponse>> getAll() {
        List<TractorResponse> tractors = tractorService.getAll();
        return ResponseEntity.ok(tractors);
    }

    @Operation(summary = "Exibir Trator", description = "Exibe os detalhes de um trator específico pelo ID")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<TractorResponse> get(@PathVariable("id") UUID id){
        TractorResponse tractorResponse = tractorService.get(id);
        return ResponseEntity.ok(tractorResponse);
    }

    @Operation(summary = "Excluir Trator", description = "Exclui um trator específico pelo ID")
    @DeleteMapping
    public ResponseEntity<Void> delete(UUID id){
        tractorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar Trator", description = "Atualiza todos os dados de um trator existente")
    @PutMapping("{id}")
    public ResponseEntity<TractorResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody TractorCreateRequest tractorCreateRequest){
        TractorResponse tractorResponse = tractorService.update(id, tractorCreateRequest);
        return ResponseEntity.ok(tractorResponse);
    }

    @Operation(summary = "Atualizar Trator Parcialmente", description = "Atualiza parcialmente os dados de um trator existente")
    @PatchMapping("{id}")
    public ResponseEntity<TractorResponse> updatePartial(@PathVariable("id") UUID id, @RequestBody TractorUpdateRequest tractorUpdateRequest){
        TractorResponse tractorResponse = tractorService.updatePartial(id, tractorUpdateRequest);
        return ResponseEntity.ok(tractorResponse);
    }
}
