package GoLogAPI.controller;

import GoLogAPI.dto.occurrence.*;
import GoLogAPI.service.OccurrenceService;
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
@RequestMapping("/occurrence")
@Tag(name = "Ocorrência")
public class OccurrenceController {

    private final OccurrenceService occurrenceService;

    public OccurrenceController(OccurrenceService occurrenceService){
        this.occurrenceService = occurrenceService;
    }

    @Operation(summary = "Cadastrar Ocorrência", description = "Cadastra uma nova ocorrência de transporte no sistema")
    @PostMapping
    public ResponseEntity<OccurrenceCreateResponse> save(@Valid @RequestBody OccurrenceCreateRequest occurrenceCreateRequest){
        OccurrenceCreateResponse ocurrenceCreateResponse = occurrenceService.save(occurrenceCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(ocurrenceCreateResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(ocurrenceCreateResponse);
    }

    @Operation(summary = "Exibir Ocorrência", description = "Exibe os detalhes de uma ocorrência específica pelo ID")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<OccurrenceResponse> get(@PathVariable("id") UUID id){
        OccurrenceResponse occurrenceResponse = occurrenceService.get(id);
        return ResponseEntity.ok().body(occurrenceResponse);
    }

    @Operation(summary = "Listar Ocorrências", description = "Retorna uma lista de todas as ocorrências cadastradas")
    @GetMapping
    public ResponseEntity<List<OccurrenceResponseList>> getAll(){
        List<OccurrenceResponseList> occurrences = occurrenceService.getAll();
        return ResponseEntity.ok().body(occurrences);
    }

    @Operation(summary = "Excluir Ocorrência", description = "Exclui uma ocorrência específica pelo ID")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        occurrenceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar Ocorrência", description = "Atualiza todos os dados de uma ocorrência existente")
    @PutMapping("{id}")
    public ResponseEntity<OccurrenceCreateResponse> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody OccurrenceCreateRequest occurrenceCreateRequest)
    {
        OccurrenceCreateResponse occurrenceCreateResponse = occurrenceService.update(id, occurrenceCreateRequest);
        return ResponseEntity.ok().body(occurrenceCreateResponse);
    }

    @Operation(summary = "Atualizar Ocorrência Parcialmente", description = "Atualiza parcialmente os dados de uma ocorrência existente")
    @PatchMapping("{id}")
    public ResponseEntity<OccurrenceCreateResponse> updatePartial(
            @PathVariable("id") UUID id,
            @Valid @RequestBody OccurrenceUpdateRequest occurrenceUpdateRequest)
    {
        OccurrenceCreateResponse occurrenceCreateResponse = occurrenceService.updatePartial(id, occurrenceUpdateRequest);
        return ResponseEntity.ok().body(occurrenceCreateResponse);
    }

}
