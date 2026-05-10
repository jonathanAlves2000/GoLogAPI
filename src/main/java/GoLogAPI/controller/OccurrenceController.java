package GoLogAPI.controller;

import GoLogAPI.dto.occurrence.*;
import GoLogAPI.service.OccurrenceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/occurrence")
public class OccurrenceController {

    private final OccurrenceService occurrenceService;

    public OccurrenceController(OccurrenceService occurrenceService){
        this.occurrenceService = occurrenceService;
    }

    @PostMapping
    public ResponseEntity<OccurrenceCreateResponse> save(@Valid @RequestBody OccurrenceCreateRequest occurrenceCreateRequest){
        OccurrenceCreateResponse ocurrenceCreateResponse = occurrenceService.save(occurrenceCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(ocurrenceCreateResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(ocurrenceCreateResponse);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<OccurrenceResponse> get(@PathVariable("id") UUID id){
        OccurrenceResponse occurrenceResponse = occurrenceService.get(id);
        return ResponseEntity.ok().body(occurrenceResponse);
    }

    @GetMapping
    public ResponseEntity<List<OccurrenceResponseList>> getAll(){
        List<OccurrenceResponseList> occurrences = occurrenceService.getAll();
        return ResponseEntity.ok().body(occurrences);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        occurrenceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<OccurrenceCreateResponse> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody OccurrenceCreateRequest occurrenceCreateRequest)
    {
        OccurrenceCreateResponse occurrenceCreateResponse = occurrenceService.update(id, occurrenceCreateRequest);
        return ResponseEntity.ok().body(occurrenceCreateResponse);
    }

    @PatchMapping("{id}")
    public ResponseEntity<OccurrenceCreateResponse> updatePartial(
            @PathVariable("id") UUID id,
            @Valid @RequestBody OccurrenceUpdateRequest occurrenceUpdateRequest)
    {
        OccurrenceCreateResponse occurrenceCreateResponse = occurrenceService.updatePartial(id, occurrenceUpdateRequest);
        return ResponseEntity.ok().body(occurrenceCreateResponse);
    }

}
