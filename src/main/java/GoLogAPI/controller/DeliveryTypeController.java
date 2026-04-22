package GoLogAPI.controller;

import GoLogAPI.dto.deliveryType.DeliveryTypeCreateRequest;
import GoLogAPI.dto.deliveryType.DeliveryTypeRequest;
import GoLogAPI.dto.deliveryType.DeliveryTypeResponse;
import GoLogAPI.dto.deliveryType.DeliveryTypeUpdateRequest;
import GoLogAPI.service.DeliveryTypeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/delivery-type")
public class DeliveryTypeController {

    private final DeliveryTypeService deliveryTypeService;

    public DeliveryTypeController(DeliveryTypeService deliveryTypeService){
        this.deliveryTypeService = deliveryTypeService;
    }

    @PostMapping
    public ResponseEntity<DeliveryTypeResponse> save(@Valid @RequestBody DeliveryTypeCreateRequest deliveryTypeCreateRequest){
        DeliveryTypeResponse deliveryTypeResponse = deliveryTypeService.save(deliveryTypeCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand()
                .toUri();
        return ResponseEntity.created(uri).body(deliveryTypeResponse);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<DeliveryTypeResponse> get(@PathVariable("id") UUID id){
        DeliveryTypeResponse deliveryTypeResponse = deliveryTypeService.get(id);
        return ResponseEntity.ok(deliveryTypeResponse);
    }

    @DeleteMapping(value = "{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        deliveryTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(value = "{id}")
    public ResponseEntity<DeliveryTypeResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody DeliveryTypeCreateRequest deliveryTypeCreateRequest){
        DeliveryTypeResponse deliveryTypeResponse = deliveryTypeService.update(id, deliveryTypeCreateRequest);
        return ResponseEntity.ok(deliveryTypeResponse);
    }

    @PatchMapping(value = "{id}")
    public ResponseEntity<DeliveryTypeResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody DeliveryTypeUpdateRequest deliveryTypeUpdateRequest){
        DeliveryTypeResponse deliveryTypeResponse = deliveryTypeService.updatePartial(id, deliveryTypeUpdateRequest);
        return ResponseEntity.ok(deliveryTypeResponse);
    }

}
