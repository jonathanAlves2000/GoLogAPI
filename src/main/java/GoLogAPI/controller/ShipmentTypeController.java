package GoLogAPI.controller;

import GoLogAPI.dto.shipmentType.DeliveryTypeCreateRequest;
import GoLogAPI.dto.shipmentType.DeliveryTypeResponse;
import GoLogAPI.dto.shipmentType.DeliveryTypeUpdateRequest;
import GoLogAPI.service.ShipmentTypeService;
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
@RequestMapping("/shipment-type")
@Tag(name = "Tipo de Carga")
public class ShipmentTypeController {

    private final ShipmentTypeService shipmentTypeService;

    public ShipmentTypeController(ShipmentTypeService shipmentTypeService){
        this.shipmentTypeService = shipmentTypeService;
    }

    @Operation(summary = "Cadastrar Tipo de Carga", description = "Cadastra um novo tipo de carga/remessa no sistema")
    @PostMapping
    public ResponseEntity<DeliveryTypeResponse> save(@Valid @RequestBody DeliveryTypeCreateRequest deliveryTypeCreateRequest){
        DeliveryTypeResponse deliveryTypeResponse = shipmentTypeService.save(deliveryTypeCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(deliveryTypeResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(deliveryTypeResponse);
    }

    @Operation(summary = "Listar Tipos de Carga", description = "Retorna uma lista de todos os tipos de carga/remessa cadastrados")
    @GetMapping
    public ResponseEntity<List<DeliveryTypeResponse>> getAll() {
        List<DeliveryTypeResponse> deliveryTypes = shipmentTypeService.getAll();
        return ResponseEntity.ok(deliveryTypes);
    }

    @Operation(summary = "Exibir Tipo de Carga", description = "Exibe os detalhes de um tipo de carga/remessa específico pelo ID")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<DeliveryTypeResponse> get(@PathVariable("id") UUID id){
        DeliveryTypeResponse deliveryTypeResponse = shipmentTypeService.get(id);
        return ResponseEntity.ok(deliveryTypeResponse);
    }

    @Operation(summary = "Excluir Tipo de Carga", description = "Exclui um tipo de carga/remessa específico pelo ID")
    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        shipmentTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar Tipo de Carga", description = "Atualiza todos os dados de um tipo de carga/remessa existente")
    @PutMapping(value = "{id}")
    public ResponseEntity<DeliveryTypeResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody DeliveryTypeCreateRequest deliveryTypeCreateRequest){
        DeliveryTypeResponse deliveryTypeResponse = shipmentTypeService.update(id, deliveryTypeCreateRequest);
        return ResponseEntity.ok(deliveryTypeResponse);
    }

    @Operation(summary = "Atualizar Tipo de Carga Parcialmente", description = "Atualiza parcialmente os dados de um tipo de carga/remessa existente")
    @PatchMapping(value = "{id}")
    public ResponseEntity<DeliveryTypeResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody DeliveryTypeUpdateRequest deliveryTypeUpdateRequest){
        DeliveryTypeResponse deliveryTypeResponse = shipmentTypeService.updatePartial(id, deliveryTypeUpdateRequest);
        return ResponseEntity.ok(deliveryTypeResponse);
    }

}
