package GoLogAPI.controller;

import GoLogAPI.dto.workShiftTemplate.WorkShiftTemplateCreateRequest;
import GoLogAPI.dto.workShiftTemplate.WorkShiftTemplateResponse;
import GoLogAPI.service.WorkShiftTemplateService;
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
@RequestMapping({"/work-shift-template", "/workShiftTemplate"})
@Tag(name = "Modelos de Jornada de Trabalho", description = "Gerenciamento de turnos pré-definidos e jornadas de motoristas (Lei 13.103)")
public class WorkShiftTemplateController {

    private final WorkShiftTemplateService workShiftTemplateService;

    public WorkShiftTemplateController(WorkShiftTemplateService workShiftTemplateService) {
        this.workShiftTemplateService = workShiftTemplateService;
    }

    @Operation(summary = "Listar modelos de jornada", description = "Retorna todos os modelos de turnos (da empresa e globais pré-definidos)")
    @GetMapping
    public ResponseEntity<List<WorkShiftTemplateResponse>> getAll() {
        return ResponseEntity.ok(workShiftTemplateService.getAll());
    }

    @Operation(summary = "Buscar modelo por ID", description = "Retorna detalhes de um modelo de jornada")
    @GetMapping("/{id}")
    public ResponseEntity<WorkShiftTemplateResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(workShiftTemplateService.getById(id));
    }

    @Operation(summary = "Cadastrar modelo de jornada", description = "Cria um novo modelo de jornada de trabalho para a empresa")
    @PostMapping
    public ResponseEntity<WorkShiftTemplateResponse> create(@Valid @RequestBody WorkShiftTemplateCreateRequest request) {
        WorkShiftTemplateResponse created = workShiftTemplateService.save(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Atualizar modelo de jornada", description = "Atualiza os parâmetros de um modelo de jornada existente")
    @PutMapping("/{id}")
    public ResponseEntity<WorkShiftTemplateResponse> update(@PathVariable UUID id, @Valid @RequestBody WorkShiftTemplateCreateRequest request) {
        return ResponseEntity.ok(workShiftTemplateService.update(id, request));
    }

    @Operation(summary = "Remover modelo de jornada", description = "Exclui um modelo de jornada da empresa")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        workShiftTemplateService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
