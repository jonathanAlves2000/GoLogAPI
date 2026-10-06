package GoLogAPI.controller;

import GoLogAPI.dto.demandType.DemandTypeCreateRequest;
import GoLogAPI.dto.demandType.DemandTypeResponse;
import GoLogAPI.dto.demandType.DemandTypeUpdateRequest;
import GoLogAPI.service.DemandTypeService;
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
@RequestMapping("/demand-type")
@Tag(name = "Tipos de Demanda", description = "Gerenciamento de tipos de demanda dinâmicos (peso, volume, paletes, etc.)")
public class DemandTypeController {

    private final DemandTypeService demandTypeService;

    public DemandTypeController(DemandTypeService demandTypeService) {
        this.demandTypeService = demandTypeService;
    }

    @Operation(summary = "Cadastrar Tipo de Demanda", description = "Cadastra um novo tipo de demanda customizado")
    @PostMapping
    public ResponseEntity<DemandTypeResponse> save(@Valid @RequestBody DemandTypeCreateRequest request) {
        DemandTypeResponse response = demandTypeService.save(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(summary = "Listar Tipos de Demanda", description = "Retorna todos os tipos de demanda cadastrados")
    @GetMapping
    public ResponseEntity<List<DemandTypeResponse>> getAll() {
        return ResponseEntity.ok(demandTypeService.getAll());
    }

    @Operation(summary = "Listar Tipos de Demanda Disponíveis por Empresa", description = "Retorna os tipos de demanda padrão do sistema e os específicos da empresa")
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<DemandTypeResponse>> getAvailableForCompany(@PathVariable("companyId") UUID companyId) {
        return ResponseEntity.ok(demandTypeService.getAvailableForCompany(companyId));
    }

    @Operation(summary = "Exibir Tipo de Demanda", description = "Exibe os detalhes de um tipo de demanda pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<DemandTypeResponse> get(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(demandTypeService.get(id));
    }

    @Operation(summary = "Excluir Tipo de Demanda", description = "Exclui um tipo de demanda customizado (não permite excluir padrões do sistema)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        demandTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar Tipo de Demanda", description = "Atualiza os dados de um tipo de demanda")
    @PutMapping("/{id}")
    public ResponseEntity<DemandTypeResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody DemandTypeUpdateRequest request) {
        return ResponseEntity.ok(demandTypeService.update(id, request));
    }
}
