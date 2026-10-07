package GoLogAPI.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import GoLogAPI.dto.telemetry.TelemetryBatchRequest;
import GoLogAPI.dto.telemetry.TelemetryBatchResponse;
import GoLogAPI.dto.telemetry.TelemetryResponseList;
import GoLogAPI.service.telemetry.TelemetryBatchService;
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
@RequestMapping({"/api/v1/telemetry", "/telemetry"})
@Tag(name = "Telemetria & Ingestion Gateway", description = "Serviços de ingestão de telemetria de alta performance e geofencing de rotas")
public class TelemetryController {

    private final TelemetryService telemetryService;
    private final TelemetryBatchService telemetryBatchService;

    public TelemetryController(TelemetryService telemetryService,
                               TelemetryBatchService telemetryBatchService) {
        this.telemetryService = telemetryService;
        this.telemetryBatchService = telemetryBatchService;
    }

    @Operation(summary = "Ingestão de Telemetria em Lote (Batch Gateway)",
               description = "Endpoint de alta performance para frotistas e rastreadores IoT. Resolução de placas em memória e detecção automática de chegada via Geofencing.")
    @PostMapping("/batch")
    public ResponseEntity<TelemetryBatchResponse> processBatch(@RequestBody @Valid TelemetryBatchRequest batchRequest) {
        TelemetryBatchResponse response = telemetryBatchService.processBatch(batchRequest.items());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cadastrar Telemetria", description = "Cadastra uma nova leitura de telemetria no sistema")
    @PostMapping
    public ResponseEntity<TelemetryReponse> save(@RequestBody @Valid TelemetryCreateRequest telemetryCreateRequest){
        TelemetryReponse telemetryReponse = telemetryService.save(telemetryCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
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
