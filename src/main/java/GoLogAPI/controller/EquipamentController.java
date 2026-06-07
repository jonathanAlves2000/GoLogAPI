package GoLogAPI.controller;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import GoLogAPI.dto.equipament.EquipamentResponse;
import GoLogAPI.service.EquipamentService;

@RestController
@RequestMapping("/equipament")
@Tag(name = "Equipamento")
public class EquipamentController {

    private final EquipamentService equipamentService;

    public EquipamentController(EquipamentService equipamentService){
        this.equipamentService = equipamentService;
    }

    @Operation(summary = "Exibir Equipamento", description = "Exibe os detalhes de um equipamento específico pelo ID")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<EquipamentResponse> get(@PathVariable("id") UUID id){
        EquipamentResponse equipamentResponse = equipamentService.get(id);
        return ResponseEntity.ok(equipamentResponse);
    }

    @Operation(summary = "Listar Equipamentos", description = "Retorna uma lista de todos os equipamentos cadastrados")
    @GetMapping
    public ResponseEntity<List<EquipamentResponse>> getAll(){
        List<EquipamentResponse> equipaments = equipamentService.getAll();
        return ResponseEntity.ok().body(equipaments);
    }
}
