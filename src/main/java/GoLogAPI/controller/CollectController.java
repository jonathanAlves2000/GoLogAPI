package GoLogAPI.controller;

import GoLogAPI.dto.collect.*;
import GoLogAPI.service.CollectService;
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
@RequestMapping("/collect")
@Tag(name = "Collect")
public class CollectController {

    private final CollectService collectService;

    public CollectController(CollectService collectService){
        this.collectService = collectService;
    }

    @Operation(summary = "Create", description = "Create New Collect")
    @PostMapping
    public ResponseEntity<CollectCreateResponse> save(@Valid @RequestBody CollectCreateRequest collectCreateRequest){
        CollectCreateResponse collectCreateResponse = collectService.save(collectCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(collectCreateResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(collectCreateResponse);
    }

    @Operation(summary = "Display", description = "Display Collect")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<CollectResponse> get(@PathVariable("id") UUID id){
        CollectResponse collectResponse = collectService.get(id);
        return ResponseEntity.ok(collectResponse);
    }

    @Operation(summary = "Display List", description = "Display Collect List")
    @GetMapping
    public ResponseEntity<List<CollectResponseList>> getAll() {
        List<CollectResponseList> collectResponses = collectService.getAll();
        return ResponseEntity.ok().body(collectResponses);
    }

    @Operation(summary = "Delete", description = "Delete Collect")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        collectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update", description = "Update Collect")
    @PutMapping("{id}")
    public ResponseEntity<CollectCreateResponse> update(
            @PathVariable("id") UUID id, @Valid @RequestBody CollectCreateRequest collectCreateRequest){
        CollectCreateResponse collectCreateResponse = collectService.update(id, collectCreateRequest);
        return ResponseEntity.ok().body(collectCreateResponse);
    }

    @Operation(summary = "Update Partial", description = "Update Collect Partial")
    @PatchMapping("{id}")
    public ResponseEntity<CollectCreateResponse> updatePartial(
            @PathVariable("id") UUID id, @Valid @RequestBody CollectUpdateRequest collectUpdateRequest){
        CollectCreateResponse collectCreateResponse = collectService.updatePartial(id, collectUpdateRequest);
        return ResponseEntity.ok().body(collectCreateResponse);
    }
}
