package GoLogAPI.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import GoLogAPI.dto.telemetry.TelemetryResponseList;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import GoLogAPI.dto.telemetry.TelemetryCreateRequest;
import GoLogAPI.dto.telemetry.TelemetryReponse;
import GoLogAPI.dto.telemetry.TelemetryUpdateRequest;
import GoLogAPI.service.TelemetryService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/telemetry")
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController( TelemetryService telemetryService){
        this.telemetryService = telemetryService;
    }

    @PostMapping
    public ResponseEntity<TelemetryReponse> save(@RequestBody @Valid TelemetryCreateRequest telemetryCreateRequest){
        TelemetryReponse telemetryReponse = telemetryService.save(telemetryCreateRequest);
           URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(telemetryReponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(telemetryReponse);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<TelemetryReponse> get(@PathVariable("id") UUID id){
        TelemetryReponse telemetryReponse = telemetryService.get(id);
        return ResponseEntity.ok(telemetryReponse);
    }

    @Operation(summary = "Get List", description = "List Telemeteria")
    @GetMapping
    public ResponseEntity<List<TelemetryResponseList>> getAll(){
        List<TelemetryResponseList> telemetries = telemetryService.getAll();
        return ResponseEntity.ok().body(telemetries);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        telemetryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping
    public ResponseEntity<TelemetryReponse> update(@PathVariable("id") UUID id, @Valid TelemetryCreateRequest telemetryCreateRequest){
        TelemetryReponse telemetryReponse = telemetryService.update(id, telemetryCreateRequest);
        return ResponseEntity.ok(telemetryReponse);
    }

    @PatchMapping("{id}")
    public ResponseEntity<TelemetryReponse> updatePartial(@PathVariable("id") UUID id, TelemetryUpdateRequest telemetryUpdateRequest){
        TelemetryReponse telemetryReponse = telemetryService.updatePartial(id, telemetryUpdateRequest);
        return ResponseEntity.ok(telemetryReponse);
    }
}
