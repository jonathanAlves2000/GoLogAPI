package GoLogAPI.controller;

import GoLogAPI.service.EquipamentGroupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import GoLogAPI.dto.equipamentGroup.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.UUID;

@Controller
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
    public ResponseEntity<EquipamentGroupResponse> get(@PathVariable UUID id){
       EquipamentGroupResponse equipamentGroupResponse = equipamentGroupService.get(id);
       return ResponseEntity.ok(equipamentGroupResponse);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        equipamentGroupService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<EquipamentGroupResponse> update(
            @PathVariable UUID id, @RequestBody EquipamentGroupCreateRequest equipamentGroupCreateRequest){
        EquipamentGroupResponse equipamentGroupResponse = equipamentGroupService.update(id, equipamentGroupCreateRequest);
        return ResponseEntity.ok(equipamentGroupResponse);
    }

    @PatchMapping("{id}")
    public ResponseEntity<EquipamentGroupResponse> updatePartial(
            @PathVariable UUID id, @RequestBody EquipamentGroupPatchRequest equipamentGroupPatchRequest){
        EquipamentGroupResponse equipamentGroupResponse = equipamentGroupService.updatePartial(id, equipamentGroupPatchRequest);
        return ResponseEntity.ok(equipamentGroupResponse);
    }

}
