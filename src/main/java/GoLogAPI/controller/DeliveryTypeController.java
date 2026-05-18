package GoLogAPI.controller;

import GoLogAPI.dto.deliveryType.DeliveryTypeCreateRequest;
import GoLogAPI.dto.deliveryType.DeliveryTypeResponse;
import GoLogAPI.dto.deliveryType.DeliveryTypeUpdateRequest;
import GoLogAPI.service.DeliveryTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/delivery-type")
@Tag(name = "Delivery Type")
public class DeliveryTypeController {

    private final DeliveryTypeService deliveryTypeService;

    public DeliveryTypeController(DeliveryTypeService deliveryTypeService){
        this.deliveryTypeService = deliveryTypeService;
    }

    @Operation(summary = "Create", description = "Create New Delivery Type")
    @PostMapping
    public ResponseEntity<DeliveryTypeResponse> save(@Valid @RequestBody DeliveryTypeCreateRequest deliveryTypeCreateRequest){
        DeliveryTypeResponse deliveryTypeResponse = deliveryTypeService.save(deliveryTypeCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(deliveryTypeResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(deliveryTypeResponse);
    }

    @Operation(summary = "Display", description = "Display Delivery Type")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<DeliveryTypeResponse> get(@PathVariable("id") UUID id){
        DeliveryTypeResponse deliveryTypeResponse = deliveryTypeService.get(id);
        return ResponseEntity.ok(deliveryTypeResponse);
    }

    @Operation(summary = "Delete", description = "Delete Delivery Type")
    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        deliveryTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update", description = "Update Delivery Type")
    @PutMapping(value = "{id}")
    public ResponseEntity<DeliveryTypeResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody DeliveryTypeCreateRequest deliveryTypeCreateRequest){
        DeliveryTypeResponse deliveryTypeResponse = deliveryTypeService.update(id, deliveryTypeCreateRequest);
        return ResponseEntity.ok(deliveryTypeResponse);
    }

    @Operation(summary = "Update Partial", description = "Update Partial Delivery Type")
    @PatchMapping(value = "{id}")
    public ResponseEntity<DeliveryTypeResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody DeliveryTypeUpdateRequest deliveryTypeUpdateRequest){
        DeliveryTypeResponse deliveryTypeResponse = deliveryTypeService.updatePartial(id, deliveryTypeUpdateRequest);
        return ResponseEntity.ok(deliveryTypeResponse);
    }

}
