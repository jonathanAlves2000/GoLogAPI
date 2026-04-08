package GoLogAPI.controller;

import GoLogAPI.dto.trailer.TrailerCreateRequest;
import GoLogAPI.dto.trailer.TrailerPatchRequest;
import GoLogAPI.dto.trailer.TrailerResponse;
import GoLogAPI.service.TrailerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/trailer")
public class TrailerController {

    TrailerService trailerService;

    public TrailerController(TrailerService trailerService){
        this.trailerService = trailerService;
    }

    @PostMapping
    public ResponseEntity<TrailerResponse> save(@Valid @RequestBody TrailerCreateRequest trailerCreateRequest){
        TrailerResponse trailerResponse = trailerService.save(trailerCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(trailerResponse.getId())
                .toUri();
        return ResponseEntity.created(uri).body(trailerResponse);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<TrailerResponse> get(@PathVariable("id") UUID id){
        TrailerResponse trailerResponse = trailerService.get(id);
        return ResponseEntity.ok().body(trailerResponse);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        trailerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<TrailerResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody TrailerCreateRequest trailerCreateRequest){
        TrailerResponse trailerResponse = trailerService.update(id, trailerCreateRequest);
        return ResponseEntity.ok().body(trailerResponse);
    }

    @PatchMapping("{id}")
    public ResponseEntity<TrailerResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody TrailerPatchRequest trailerPatchRequest){
        TrailerResponse trailerResponse = trailerService.updatePartial(id, trailerPatchRequest);
        return ResponseEntity.ok().body(trailerResponse);
    }

}
