package GoLogAPI.controller;
import GoLogAPI.dto.delivery.DeliveryCreateRequest;
import GoLogAPI.dto.delivery.DeliveryCreateResponse;
import GoLogAPI.dto.delivery.DeliveryResponse;
import GoLogAPI.dto.delivery.DeliveryUpdateRequest;
import GoLogAPI.service.DeliveryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService){
        this.deliveryService = deliveryService;
    }

    @PostMapping
    public ResponseEntity<DeliveryCreateResponse> save(@Valid @RequestBody DeliveryCreateRequest deliveryCreateRequest){
        DeliveryCreateResponse deliveryCreateResponse = deliveryService.save(deliveryCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(deliveryCreateResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(deliveryCreateResponse);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<DeliveryResponse> get(@PathVariable("id") UUID id){
        DeliveryResponse deliveryResponse = deliveryService.get(id);
        return ResponseEntity.ok(deliveryResponse);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        deliveryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<DeliveryCreateResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody DeliveryCreateRequest deliveryCreateRequest){
        DeliveryCreateResponse deliveryCreateResponse = deliveryService.update(id, deliveryCreateRequest);
        return ResponseEntity.ok().body(deliveryCreateResponse);
    }

    @PatchMapping("{id}")
    public ResponseEntity<DeliveryCreateResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody DeliveryUpdateRequest deliveryUpdateRequest){
        DeliveryCreateResponse deliveryCreateResponse = deliveryService.updatePartial(id, deliveryUpdateRequest);
        return ResponseEntity.ok().body(deliveryCreateResponse);
    }
}
