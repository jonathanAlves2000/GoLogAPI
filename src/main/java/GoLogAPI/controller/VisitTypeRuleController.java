package GoLogAPI.controller;

import GoLogAPI.dto.visitTypeRule.VisitTypeRuleCreateRequest;
import GoLogAPI.dto.visitTypeRule.VisitTypeRuleResponse;
import GoLogAPI.dto.visitTypeRule.VisitTypeRuleUpdateRequest;
import GoLogAPI.service.VisitTypeRuleService;
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
@RequestMapping("/visit-type-rule")
@Tag(name = "Regras de Morfologia / Compatibilidade", description = "Gerenciamento de regras de incompatibilidade e requisitos entre visitas")
public class VisitTypeRuleController {

    private final VisitTypeRuleService visitTypeRuleService;

    public VisitTypeRuleController(VisitTypeRuleService visitTypeRuleService) {
        this.visitTypeRuleService = visitTypeRuleService;
    }

    @Operation(summary = "Cadastrar Regra de Morfologia", description = "Cadastra uma regra de incompatibilidade ou exigência entre tipos de visita")
    @PostMapping
    public ResponseEntity<VisitTypeRuleResponse> save(@Valid @RequestBody VisitTypeRuleCreateRequest request) {
        VisitTypeRuleResponse response = visitTypeRuleService.save(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(summary = "Listar Regras por Empresa", description = "Retorna todas as regras de morfologia cadastradas para a empresa")
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<VisitTypeRuleResponse>> getByCompany(@PathVariable("companyId") UUID companyId) {
        return ResponseEntity.ok(visitTypeRuleService.getByCompany(companyId));
    }

    @Operation(summary = "Exibir Regra", description = "Exibe os detalhes de uma regra pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<VisitTypeRuleResponse> get(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(visitTypeRuleService.get(id));
    }

    @Operation(summary = "Excluir Regra", description = "Exclui uma regra de morfologia pelo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        visitTypeRuleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar Regra", description = "Atualiza os dados de uma regra existente")
    @PutMapping("/{id}")
    public ResponseEntity<VisitTypeRuleResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody VisitTypeRuleUpdateRequest request) {
        return ResponseEntity.ok(visitTypeRuleService.update(id, request));
    }
}
