package GoLogAPI.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/equipamentGroup")
public class EquipamentGroupController {

    EquipamentGroupService equipamentGroupService;

    public EquipamentGroupController(EquipamentGroupService equipamentGroupService){
        this.equipamentGroupService = equipamentGroupService;
    }

    @PostMapping
    public ResponseEntity<EquipamentGroupResponse> save(@Valid @RequestBody EquipamentGroupCreateRequest equipamentGroupCreateRequest){
        EquipamentGroupResponse equipamentGroupResponse = equipamentGroupService.save(equipamentGroupCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(equipamentGroupResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(equipamentGroupResponse);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<EquipamentGroupResponse> get(@PathVariable("id") UUID id){
       EquipamentGroupResponse equipamentGroupResponse = equipamentGroupService.get(id);
       return ResponseEntity.ok(equipamentGroupResponse);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        equipamentGroupService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<EquipamentGroupResponse> update(
            @PathVariable("id") UUID id, @RequestBody EquipamentGroupCreateRequest equipamentGroupCreateRequest){
        EquipamentGroupResponse equipamentGroupResponse = equipamentGroupService.update(id, equipamentGroupCreateRequest);
        return ResponseEntity.ok(equipamentGroupResponse);
    }

    @PatchMapping("{id}")
    public ResponseEntity<EquipamentGroupResponse> updatePartial(
            @PathVariable("id") UUID id, @RequestBody EquipamentGroupUpdateRequest equipamentGroupUpdateRequest){
        EquipamentGroupResponse equipamentGroupResponse = equipamentGroupService.updatePartial(id, equipamentGroupUpdateRequest);
        return ResponseEntity.ok(equipamentGroupResponse);
    }

}
