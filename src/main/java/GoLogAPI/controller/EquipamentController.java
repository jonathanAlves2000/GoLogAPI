package GoLogAPI.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import GoLogAPI.dto.equipament.EquipamentResponse;
import GoLogAPI.service.EquipamentService;

@RestController
@RequestMapping("/equipament")
public class EquipamentController {

    private final EquipamentService equipamentService;

    public EquipamentController(EquipamentService equipamentService){
        this.equipamentService = equipamentService;
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<EquipamentResponse> get(@PathVariable("id") UUID id){
        EquipamentResponse equipamentResponse = equipamentService.get(id);
        return ResponseEntity.ok(equipamentResponse);
    }

    @GetMapping
    public ResponseEntity<List<EquipamentResponse>> getAll(){
        List<EquipamentResponse> equipaments = equipamentService.getAll();
        return ResponseEntity.ok().body(equipaments);
    }
}
