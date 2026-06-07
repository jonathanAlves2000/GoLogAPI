package GoLogAPI.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import GoLogAPI.dto.telemetry.TelemetryResponseList;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Telemetria")
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController( TelemetryService telemetryService){
        this.telemetryService = telemetryService;
    }

    @Operation(summary = "Cadastrar Telemetria", description = "Cadastra uma nova leitura de telemetria no sistema")
    @PostMapping
    public ResponseEntity<TelemetryReponse> save(@RequestBody @Valid TelemetryCreateRequest telemetryCreateRequest){
        TelemetryReponse telemetryReponse = telemetryService.save(telemetryCreateRequest);
           URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(telemetryReponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(telemetryReponse);
    }

    @Operation(summary = "Exibir Telemetria", description = "Exibe os detalhes de uma telemetria específica pelo ID")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<TelemetryReponse> get(@PathVariable("id") UUID id){
        TelemetryReponse telemetryReponse = telemetryService.get(id);
        return ResponseEntity.ok(telemetryReponse);
    }

    @Operation(summary = "Listar Telemetrias", description = "Retorna uma lista de todas as leituras de telemetria cadastradas")
    @GetMapping
    public ResponseEntity<List<TelemetryResponseList>> getAll(){
        List<TelemetryResponseList> telemetries = telemetryService.getAll();
        return ResponseEntity.ok().body(telemetries);
    }

    @Operation(summary = "Excluir Telemetria", description = "Exclui uma leitura de telemetria específica pelo ID")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        telemetryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar Telemetria", description = "Atualiza todos os dados de uma telemetria existente")
    @PutMapping("{id}")
    public ResponseEntity<TelemetryReponse> update(@PathVariable("id") UUID id, @Valid @RequestBody TelemetryCreateRequest telemetryCreateRequest){
        TelemetryReponse telemetryReponse = telemetryService.update(id, telemetryCreateRequest);
        return ResponseEntity.ok(telemetryReponse);
    }

    @Operation(summary = "Atualizar Telemetria Parcialmente", description = "Atualiza parcialmente os dados de uma telemetria existente")
    @PatchMapping("{id}")
    public ResponseEntity<TelemetryReponse> updatePartial(@PathVariable("id") UUID id, TelemetryUpdateRequest telemetryUpdateRequest){
        TelemetryReponse telemetryReponse = telemetryService.updatePartial(id, telemetryUpdateRequest);
        return ResponseEntity.ok(telemetryReponse);
    }
}
