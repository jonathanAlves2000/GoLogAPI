package GoLogAPI.controller;

import GoLogAPI.dto.tractor.TractorCreateRequest;
import GoLogAPI.dto.tractor.TractorUpdateRequest;
import GoLogAPI.dto.tractor.TractorResponse;
import GoLogAPI.service.TractorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/tractor")
public class TractorController {

    private final TractorService tractorService;

    public TractorController(TractorService tractorService){
        this.tractorService = tractorService;
    }

    @PostMapping
    public ResponseEntity<TractorResponse> save(@Valid @RequestBody TractorCreateRequest tractorCreateRequest){
        TractorResponse tractorResponse = tractorService.save(tractorCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(tractorResponse);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<TractorResponse> get(@PathVariable("id") UUID id){
        TractorResponse tractorResponse = tractorService.get(id);
        return ResponseEntity.ok(tractorResponse);
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(UUID id){
        tractorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<TractorResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody TractorCreateRequest tractorCreateRequest){
        TractorResponse tractorResponse = tractorService.update(id, tractorCreateRequest);
        return ResponseEntity.ok(tractorResponse);
    }

    @PatchMapping("{id}")
    public ResponseEntity<TractorResponse> updatePartial(@PathVariable("id") UUID id, @RequestBody TractorUpdateRequest tractorUpdateRequest){
        TractorResponse tractorResponse = tractorService.updatePartial(id, tractorUpdateRequest);
        return ResponseEntity.ok(tractorResponse);
    }
}
