package GoLogAPI.controller;
import GoLogAPI.dto.shipment.*;
import GoLogAPI.service.ShipmentService;
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
@RequestMapping("/shipment")
@Tag(name = "Carga/Remessa")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService){
        this.shipmentService = shipmentService;
    }

    @Operation(summary = "Cadastrar Carga", description = "Cadastra uma nova carga/remessa no sistema")
    @PostMapping
    public ResponseEntity<ShipmentCreateResponse> save(@Valid @RequestBody ShipmentCreateRequest shipmentCreateRequest){
        ShipmentCreateResponse shipmentCreateResponse = shipmentService.save(shipmentCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(shipmentCreateResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(shipmentCreateResponse);
    }

    @Operation(summary = "Exibir Carga", description = "Exibe os detalhes de uma carga/remessa específica pelo ID")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<ShipmentResponse> get(@PathVariable("id") UUID id){
        ShipmentResponse shipmentResponse = shipmentService.get(id);
        return ResponseEntity.ok(shipmentResponse);
    }

    @Operation(summary = "Listar Cargas", description = "Retorna uma lista de todas as cargas/remessas cadastradas")
    @GetMapping
    public ResponseEntity<List<ShipmentResponseList>> getAll(){
        List<ShipmentResponseList> deliveryResponses = shipmentService.getAll();
        return ResponseEntity.ok().body(deliveryResponses);
    }

    @Operation(summary = "Listar Cargas Personalizado", description = "Retorna uma lista personalizada de cargas/remessas com filtros de consulta")
    @GetMapping("/list-personalized")
    public ResponseEntity<List<ShipmentResponseListPersonalized>> getAllWithQuery(){
        List<ShipmentResponseListPersonalized> shipmentResponseListQueryList = shipmentService.getAllWithQuery();
        return ResponseEntity.ok(shipmentResponseListQueryList);
    }

    @Operation(summary = "Excluir Carga", description = "Exclui uma carga/remessa específica pelo ID")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        shipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar Carga", description = "Atualiza todos os dados de uma carga/remessa existente")
    @PutMapping("{id}")
    public ResponseEntity<ShipmentUpdateResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody ShipmentCreateRequest shipmentCreateRequest){
        ShipmentUpdateResponse shipmentUpdateResponse = shipmentService.update(id, shipmentCreateRequest);
        return ResponseEntity.ok().body(shipmentUpdateResponse);
    }

    @Operation(summary = "Atualizar Carga Parcialmente", description = "Atualiza parcialmente os dados de uma carga/remessa existente")
    @PatchMapping("{id}")
    public ResponseEntity<ShipmentCreateResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody ShipmentUpdateRequest shipmentUpdateRequest){
        ShipmentCreateResponse shipmentCreateResponse = shipmentService.updatePartial(id, shipmentUpdateRequest);
        return ResponseEntity.ok().body(shipmentCreateResponse);
    }
}
