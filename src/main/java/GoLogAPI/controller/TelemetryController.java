package GoLogAPI.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import GoLogAPI.dto.telemetry.TelemetryCreateRequest;
import GoLogAPI.dto.telemetry.TelemetryReponse;
import GoLogAPI.dto.telemetry.TelemetryUpdateRequest;
import GoLogAPI.service.TelemetryService;
import jakarta.validation.Valid;

@RestController
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
    public ResponseEntity<TelemetryReponse> get(@PathVariable("id") UUID id){
        TelemetryReponse telemetryReponse = telemetryService.get(id);
        return ResponseEntity.ok(telemetryReponse);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        telemetryService.delete(id);
        return ResponseEntity.ok().build();
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
