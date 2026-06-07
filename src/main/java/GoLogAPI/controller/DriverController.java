package GoLogAPI.controller;

import GoLogAPI.dto.driver.DriverCreateRequest;
import GoLogAPI.dto.driver.DriverUpdateRequest;
import GoLogAPI.dto.driver.DriverResponseList;
import GoLogAPI.dto.driver.DriverResponse;
import GoLogAPI.service.DriverService;
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
@RequestMapping("/driver")
@Tag(name = "Motorista")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService){
        this.driverService = driverService;
    }

    @Operation(summary = "Cadastrar Motorista", description = "Cadastra um novo motorista no sistema")
    @PostMapping
    public ResponseEntity<DriverResponse> save(@Valid @RequestBody DriverCreateRequest driverCreateRequest) {
        DriverResponse driverResponse = driverService.save(driverCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(driverResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(driverResponse);
    }

    @Operation(summary = "Exibir Motorista", description = "Exibe os detalhes de um motorista específico pelo ID")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<DriverResponse> get(@PathVariable("id") UUID id){
        DriverResponse driverResponse = driverService.get(id);
        return ResponseEntity.ok(driverResponse);
    }

    @Operation(summary = "Listar Motoristas", description = "Retorna uma lista de todos os motoristas cadastrados")
    @GetMapping
    public ResponseEntity<List<DriverResponseList>> getAll(){
        List<DriverResponseList> driverResponses = driverService.getAll();
        return ResponseEntity.ok(driverResponses);
    }

    @Operation(summary = "Excluir Motorista", description = "Exclui um motorista específico pelo ID")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        driverService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar Motorista", description = "Atualiza todos os dados de um motorista existente")
    @PutMapping("{id}")
    public ResponseEntity<DriverResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody DriverCreateRequest driverCreateRequest){
        DriverResponse driverResponse = driverService.update(id, driverCreateRequest);
        return ResponseEntity.ok().body(driverResponse);
    }

    @Operation(summary = "Atualizar Motorista Parcialmente", description = "Atualiza parcialmente os dados de um motorista existente")
    @PatchMapping("{id}")
    public ResponseEntity<DriverResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody DriverUpdateRequest driverUpdateRequest){
        DriverResponse driverResponse = driverService.updatePartial(id, driverUpdateRequest);
        return ResponseEntity.ok().body(driverResponse);
    }
}
