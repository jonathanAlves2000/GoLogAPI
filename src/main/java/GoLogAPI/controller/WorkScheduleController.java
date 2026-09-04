package GoLogAPI.controller;

import GoLogAPI.dto.workSchedule.WorkSheduleCreateRequest;
import GoLogAPI.dto.workSchedule.WorkSheduleResponses;
import GoLogAPI.service.WorkScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/workSchedule")
@Tag(name = "Escala de Trabalho")
public class WorkScheduleController {

    private final WorkScheduleService workScheduleService;

    public WorkScheduleController(WorkScheduleService workScheduleService){
        this.workScheduleService = workScheduleService;
    }

    @Operation(summary = "Cadastrar Escala de Trabalho", description = "Cadastra uma nova escala de trabalho")
    @PostMapping
    public ResponseEntity<Void> save(@Valid @RequestBody WorkSheduleCreateRequest workSheduleCreateRequest){
        workScheduleService.save(workSheduleCreateRequest);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "Lista Escala de Trabalho", description = "Retorna uma lista de todas as escala de trabalho")
    @GetMapping
    public ResponseEntity<List<WorkSheduleResponses>> getAll(){
        List<WorkSheduleResponses> workSheduleResponses = workScheduleService.getAll();
        return ResponseEntity.ok().body(workSheduleResponses);
    }

    @Operation(summary = "Atualizar Escala de Trabalho", description = "Atualiza todos os dados de uma escala de trabalho")
    @PutMapping("{id}")
    public ResponseEntity<Void> update(@PathVariable("id") UUID id, @Valid @RequestBody WorkSheduleCreateRequest workSheduleCreateRequest){
        workScheduleService.update(id, workSheduleCreateRequest);
        return ResponseEntity.status(204).build();
    }

    @Operation(summary = "Deletar Escala de Trabalho", description = "Esclui uma ecala de trabalho expecifica pelo ID")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        workScheduleService.delete(id);
        return ResponseEntity.status(204).build();
    }

}
