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
@Tag(name = "Shipment")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService){
        this.shipmentService = shipmentService;
    }

    @Operation(summary = "Create", description = "Create New Shipment")
    @PostMapping
    public ResponseEntity<ShipmentCreateResponse> save(@Valid @RequestBody ShipmentCreateRequest shipmentCreateRequest){
        ShipmentCreateResponse shipmentCreateResponse = shipmentService.save(shipmentCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(shipmentCreateResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(shipmentCreateResponse);
    }

    @Operation(summary = "Display", description = "Display Shipment")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<ShipmentResponse> get(@PathVariable("id") UUID id){
        ShipmentResponse shipmentResponse = shipmentService.get(id);
        return ResponseEntity.ok(shipmentResponse);
    }

    @Operation(summary = "Dispaly List", description = "Display Shipment List")
    @GetMapping
    public ResponseEntity<List<ShipmentResponseList>> getAll(){
        List<ShipmentResponseList> deliveryResponses = shipmentService.getAll();
        return ResponseEntity.ok().body(deliveryResponses);
    }

    @Operation(summary = "Delete", description = "Delete Shipment")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        shipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update", description = "Update Shipment")
    @PutMapping("{id}")
    public ResponseEntity<ShipmentCreateResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody ShipmentCreateRequest shipmentCreateRequest){
        ShipmentCreateResponse shipmentCreateResponse = shipmentService.update(id, shipmentCreateRequest);
        return ResponseEntity.ok().body(shipmentCreateResponse);
    }

    @Operation(summary = "Update", description = "Update Partial Shipment")
    @PatchMapping("{id}")
    public ResponseEntity<ShipmentCreateResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody ShipmentUpdateRequest shipmentUpdateRequest){
        ShipmentCreateResponse shipmentCreateResponse = shipmentService.updatePartial(id, shipmentUpdateRequest);
        return ResponseEntity.ok().body(shipmentCreateResponse);
    }
}
