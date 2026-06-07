package GoLogAPI.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import GoLogAPI.dto.supportedType.SupportedTypeCreateRequest;
import GoLogAPI.dto.supportedType.SupportedTypeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import GoLogAPI.dto.equipamentGroup.EquipamentGroupCreateRequest;
import GoLogAPI.dto.equipamentGroup.EquipamentGroupResponse;
import GoLogAPI.dto.equipamentGroup.EquipamentGroupUpdateRequest;
import GoLogAPI.service.EquipamentGroupService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/equipament-group")
@Tag(name = "Grupo de Equipamento")
public class EquipamentGroupController {

    private final EquipamentGroupService equipamentGroupService;

    public EquipamentGroupController(EquipamentGroupService equipamentGroupService){
        this.equipamentGroupService = equipamentGroupService;
    }

    @Operation(summary = "Cadastrar Grupo de Equipamento", description = "Cadastra um novo grupo de equipamentos no sistema")
    @PostMapping
    public ResponseEntity<EquipamentGroupResponse> save(@Valid @RequestBody EquipamentGroupCreateRequest equipamentGroupCreateRequest){
        EquipamentGroupResponse equipamentGroupResponse = equipamentGroupService.save(equipamentGroupCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(equipamentGroupResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(equipamentGroupResponse);
    }

    @Operation(summary = "Listar Grupos de Equipamento", description = "Retorna uma lista de todos os grupos de equipamentos cadastrados")
    @GetMapping
    public ResponseEntity<List<EquipamentGroupResponse>> getAll() {
        List<EquipamentGroupResponse> equipamentGroups = equipamentGroupService.getAll();
        return ResponseEntity.ok(equipamentGroups);
    }

    @Operation(summary = "Exibir Grupo de Equipamento", description = "Exibe os detalhes de um grupo de equipamentos específico pelo ID")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<EquipamentGroupResponse> get(@PathVariable("id") UUID id){
       EquipamentGroupResponse equipamentGroupResponse = equipamentGroupService.get(id);
       return ResponseEntity.ok(equipamentGroupResponse);
     }

    @Operation(summary = "Excluir Grupo de Equipamento", description = "Exclui um grupo de equipamentos específico pelo ID")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        equipamentGroupService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar Grupo de Equipamento", description = "Atualiza todos os dados de um grupo de equipamentos existente")
    @PutMapping("{id}")
    public ResponseEntity<EquipamentGroupResponse> update(
            @PathVariable("id") UUID id, @RequestBody EquipamentGroupCreateRequest equipamentGroupCreateRequest){
        EquipamentGroupResponse equipamentGroupResponse = equipamentGroupService.update(id, equipamentGroupCreateRequest);
        return ResponseEntity.ok(equipamentGroupResponse);
    }

    @Operation(summary = "Atualizar Grupo de Equipamento Parcialmente", description = "Atualiza parcialmente os dados de um grupo de equipamentos existente")
    @PatchMapping("{id}")
    public ResponseEntity<EquipamentGroupResponse> updatePartial(
            @PathVariable("id") UUID id, @RequestBody EquipamentGroupUpdateRequest equipamentGroupUpdateRequest){
        EquipamentGroupResponse equipamentGroupResponse = equipamentGroupService.updatePartial(id, equipamentGroupUpdateRequest);
        return ResponseEntity.ok(equipamentGroupResponse);
    }

    @Operation(summary = "Associar Tipo de Transporte", description = "Associa um tipo de transporte ao grupo de equipamentos")
    @PatchMapping("/association")
    public ResponseEntity<SupportedTypeResponse> addTransportType(
            @RequestBody @Valid SupportedTypeCreateRequest supportedTypeCreateRequest){
        SupportedTypeResponse supportedTypeResponse = equipamentGroupService.addTransportType(supportedTypeCreateRequest);
        return ResponseEntity.ok(supportedTypeResponse);
    }

}
