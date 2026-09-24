package GoLogAPI.controller;

import GoLogAPI.dto.equipamentCapacity.EquipamentCapacityRequest;
import GoLogAPI.dto.equipamentCapacity.EquipamentCapacityResponse;
import GoLogAPI.service.EquipamentCapacityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/equipament-capacity")
@Tag(name = "Capacidades do Equipamento", description = "Associação de limites de capacidade por tipo de demanda para equipamentos")
public class EquipamentCapacityController {

    private final EquipamentCapacityService equipamentCapacityService;

    public EquipamentCapacityController(EquipamentCapacityService equipamentCapacityService) {
        this.equipamentCapacityService = equipamentCapacityService;
    }

    @Operation(summary = "Definir Capacidades do Equipamento", description = "Associa ou atualiza a lista de limites de capacidade de um equipamento")
    @PostMapping("/equipament/{equipamentId}")
    public ResponseEntity<List<EquipamentCapacityResponse>> saveCapacities(
            @PathVariable("equipamentId") UUID equipamentId,
            @Valid @RequestBody List<EquipamentCapacityRequest> requests) {
        return ResponseEntity.ok(equipamentCapacityService.saveCapacities(equipamentId, requests));
    }

    @Operation(summary = "Listar Capacidades do Equipamento", description = "Retorna todas as capacidades cadastradas para o equipamento")
    @GetMapping("/equipament/{equipamentId}")
    public ResponseEntity<List<EquipamentCapacityResponse>> getCapacitiesByEquipamentId(@PathVariable("equipamentId") UUID equipamentId) {
        return ResponseEntity.ok(equipamentCapacityService.getCapacitiesByEquipamentId(equipamentId));
    }
}
