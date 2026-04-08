package GoLogAPI.controller;

import GoLogAPI.dto.equipament.EquipamentResponse;
import GoLogAPI.model.Equipament;
import GoLogAPI.service.EquipamentService;
import org.mapstruct.control.MappingControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/equipament")
public class EquipamentController {

    private EquipamentService equipamentService;

    public EquipamentController(EquipamentService equipamentService){
        this.equipamentService = equipamentService;
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<EquipamentResponse> get(UUID id){
        EquipamentResponse equipamentResponse = equipamentService.get(id);
        return ResponseEntity.ok(equipamentResponse);
    }
}
