package GoLogAPI.controller;

import GoLogAPI.dto.telemetry.TelemetryCreateRequest;
import GoLogAPI.dto.telemetry.TelemetryPatchRequest;
import GoLogAPI.dto.telemetry.TelemetryReponse;
import GoLogAPI.service.TelemetryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.UUID;

@Controller
@RequestMapping("/telemetry")
public class TelemetryController {

    public TelemetryService telemetryService;

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
    public ResponseEntity<TelemetryReponse> get(@PathVariable UUID id){
        TelemetryReponse telemetryReponse = telemetryService.get(id);
        return ResponseEntity.ok(telemetryReponse);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        telemetryService.delete(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<TelemetryReponse> update(@PathVariable UUID id, @Valid TelemetryCreateRequest telemetryCreateRequest){
        TelemetryReponse telemetryReponse = telemetryService.update(id, telemetryCreateRequest);
        return ResponseEntity.ok(telemetryReponse);
    }

    @PatchMapping("{id}")
    public ResponseEntity<TelemetryReponse> updatePartial(@PathVariable UUID id, TelemetryPatchRequest telemetryPatchRequest){
        TelemetryReponse telemetryReponse = telemetryService.updatePartial(id, telemetryPatchRequest);
        return ResponseEntity.ok(telemetryReponse);
    }
}
