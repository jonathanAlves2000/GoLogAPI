package GoLogAPI.controller;

import GoLogAPI.service.EquipamentGroupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import GoLogAPI.dto.equipamentGroup.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

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

//    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
//    public ResponseEntity<EquipamentGroupResponse> get(@PathVariable UUID id){
//        EquipamentGroupResponse equipamentGroupResponse = equipamentGroupService.get(id);
//        return ResponseEntity.ok(equipamentGroupResponse);
//    }

}
