package GoLogAPI.controller;

import GoLogAPI.dto.trailer.TrailerCreateRequest;
import GoLogAPI.dto.trailer.TrailerUpdateRequest;
import GoLogAPI.dto.trailer.TrailerResponse;
import GoLogAPI.service.TrailerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/trailer")
@Tag(name = "Trailer")
public class TrailerController {

    private final TrailerService trailerService;

    public TrailerController(TrailerService trailerService){
        this.trailerService = trailerService;
    }

    @Operation(summary = "Create", description = "Create New Trailer")
    @PostMapping
    public ResponseEntity<TrailerResponse> save(@Valid @RequestBody TrailerCreateRequest trailerCreateRequest){
        TrailerResponse trailerResponse = trailerService.save(trailerCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(trailerResponse.getId())
                .toUri();
        return ResponseEntity.created(uri).body(trailerResponse);
    }

    @Operation(summary = "Display", description = "Display Trailer")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<TrailerResponse> get(@PathVariable("id") UUID id){
        TrailerResponse trailerResponse = trailerService.get(id);
        return ResponseEntity.ok().body(trailerResponse);
    }

    @Operation(summary = "Delete", description = "Delete Trailer")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        trailerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update", description = "Update Trailer")
    @PutMapping("{id}")
    public ResponseEntity<TrailerResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody TrailerCreateRequest trailerCreateRequest){
        TrailerResponse trailerResponse = trailerService.update(id, trailerCreateRequest);
        return ResponseEntity.ok().body(trailerResponse);
    }

    @Operation(summary = "Update Partial", description = "Update Partial Trailer")
    @PatchMapping("{id}")
    public ResponseEntity<TrailerResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody TrailerUpdateRequest trailerUpdateRequest){
        TrailerResponse trailerResponse = trailerService.updatePartial(id, trailerUpdateRequest);
        return ResponseEntity.ok().body(trailerResponse);
    }

}
