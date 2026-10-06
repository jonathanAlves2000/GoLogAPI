package GoLogAPI.controller;

import GoLogAPI.dto.visitType.VisitTypeCreateRequest;
import GoLogAPI.dto.visitType.VisitTypeResponse;
import GoLogAPI.dto.visitType.VisitTypeUpdateRequest;
import GoLogAPI.service.VisitTypeService;
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
@RequestMapping("/visit-type")
@Tag(name = "Tipos de Visita / Morfologia", description = "Gerenciamento de tipos de visita e características morfológicas")
public class VisitTypeController {

    private final VisitTypeService visitTypeService;

    public VisitTypeController(VisitTypeService visitTypeService) {
        this.visitTypeService = visitTypeService;
    }

    @Operation(summary = "Cadastrar Tipo de Visita", description = "Cadastra um novo tipo de visita/morfologia")
    @PostMapping
    public ResponseEntity<VisitTypeResponse> save(@Valid @RequestBody VisitTypeCreateRequest request) {
        VisitTypeResponse response = visitTypeService.save(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(summary = "Listar Tipos de Visita", description = "Retorna todos os tipos de visita cadastrados")
    @GetMapping
    public ResponseEntity<List<VisitTypeResponse>> getAll() {
        return ResponseEntity.ok(visitTypeService.getAll());
    }

    @Operation(summary = "Listar Tipos de Visita por Empresa", description = "Retorna os tipos de visita disponíveis para uma empresa")
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<VisitTypeResponse>> getAvailableForCompany(@PathVariable("companyId") UUID companyId) {
        return ResponseEntity.ok(visitTypeService.getAvailableForCompany(companyId));
    }

    @Operation(summary = "Exibir Tipo de Visita", description = "Exibe os detalhes de um tipo de visita pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<VisitTypeResponse> get(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(visitTypeService.get(id));
    }

    @Operation(summary = "Excluir Tipo de Visita", description = "Exclui um tipo de visita pelo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        visitTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar Tipo de Visita", description = "Atualiza os dados de um tipo de visita existente")
    @PutMapping("/{id}")
    public ResponseEntity<VisitTypeResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody VisitTypeUpdateRequest request) {
        return ResponseEntity.ok(visitTypeService.update(id, request));
    }
}
