package GoLogAPI.controller;

import GoLogAPI.dto.occurrence.*;
import GoLogAPI.service.OccurrenceService;
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
@RequestMapping("/occurrence")
@Tag(name = "Occurrence")
public class OccurrenceController {

    private final OccurrenceService occurrenceService;

    public OccurrenceController(OccurrenceService occurrenceService){
        this.occurrenceService = occurrenceService;
    }

    @Operation(summary = "Create", description = "Create New Occurrence")
    @PostMapping
    public ResponseEntity<OccurrenceCreateResponse> save(@Valid @RequestBody OccurrenceCreateRequest occurrenceCreateRequest){
        OccurrenceCreateResponse ocurrenceCreateResponse = occurrenceService.save(occurrenceCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(ocurrenceCreateResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(ocurrenceCreateResponse);
    }

    @Operation(summary = "Display", description = "Display Occurrence")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<OccurrenceResponse> get(@PathVariable("id") UUID id){
        OccurrenceResponse occurrenceResponse = occurrenceService.get(id);
        return ResponseEntity.ok().body(occurrenceResponse);
    }

    @Operation(summary = "Display List", description = "Display List Occurrence")
    @GetMapping
    public ResponseEntity<List<OccurrenceResponseList>> getAll(){
        List<OccurrenceResponseList> occurrences = occurrenceService.getAll();
        return ResponseEntity.ok().body(occurrences);
    }

    @Operation(summary = "Delete", description = "Delete Occurrence")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        occurrenceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update", description = "Update Occurrence")
    @PutMapping("{id}")
    public ResponseEntity<OccurrenceCreateResponse> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody OccurrenceCreateRequest occurrenceCreateRequest)
    {
        OccurrenceCreateResponse occurrenceCreateResponse = occurrenceService.update(id, occurrenceCreateRequest);
        return ResponseEntity.ok().body(occurrenceCreateResponse);
    }

    @Operation(summary = "Update Partial", description = "Update Partial Occurrence")
    @PatchMapping("{id}")
    public ResponseEntity<OccurrenceCreateResponse> updatePartial(
            @PathVariable("id") UUID id,
            @Valid @RequestBody OccurrenceUpdateRequest occurrenceUpdateRequest)
    {
        OccurrenceCreateResponse occurrenceCreateResponse = occurrenceService.updatePartial(id, occurrenceUpdateRequest);
        return ResponseEntity.ok().body(occurrenceCreateResponse);
    }

}
