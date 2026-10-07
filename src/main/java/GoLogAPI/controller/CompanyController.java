package GoLogAPI.controller;

import GoLogAPI.dto.company.*;
import GoLogAPI.service.CompanyService;
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
@RequestMapping("/company")
@Tag(name = "Empresas & Parceiros")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService){
        this.companyService = companyService;
    }

    @Operation(summary = "Cadastrar Empresa", description = "Cadastra uma nova empresa no sistema")
    @PostMapping
    public ResponseEntity<CompanyCreateResponse> save(@Valid @RequestBody CompanyCreateRequest companyCreateRequest){
        CompanyCreateResponse companyCreateResponse = companyService.save(companyCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(companyCreateResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(companyCreateResponse);
    }

    @Operation(summary = "Exibir Empresa", description = "Exibe os detalhes de uma empresa específica pelo ID")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<CompanyResponse> get(@PathVariable("id") UUID id){
        CompanyResponse companyResponse = companyService.get(id);
        return ResponseEntity.ok(companyResponse);
    }

    @Operation(summary = "Listar Empresas", description = "Retorna uma lista de todas as empresas cadastradas")
    @GetMapping
    public ResponseEntity<List<CompanyResponseList>> getAll(){
        List<CompanyResponseList> companyResponses = companyService.getAll();
        return ResponseEntity.ok().body(companyResponses);
    }

    @Operation(summary = "Excluir Empresa", description = "Exclui uma empresa específica pelo ID")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar Empresa", description = "Atualiza todos os dados de uma empresa existente")
    @PutMapping("{id}")
    public ResponseEntity<CompanyCreateResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody CompanyCreateRequest companyCreateRequest){
        CompanyCreateResponse companyCreateResponse = companyService.update(id, companyCreateRequest);
        return ResponseEntity.ok().body(companyCreateResponse);
    }

    @Operation(summary = "Atualizar Empresa Parcialmente", description = "Atualiza parcialmente os dados de uma empresa existente")
    @PatchMapping("{id}")
    public ResponseEntity<CompanyCreateResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody CompanyUpdateRequest companyUpdateRequest){
        CompanyCreateResponse companyCreateResponse = companyService.updatePartial(id, companyUpdateRequest);
        return ResponseEntity.ok().body(companyCreateResponse);
    }

    @Operation(summary = "Configurar Webhook de Integração", description = "Define ou atualiza a URL de callback e chave secreta para envio de eventos de entrega para o ERP")
    @PatchMapping("/{id}/webhook")
    public ResponseEntity<CompanyResponse> updateWebhook(
            @PathVariable("id") UUID id,
            @Valid @RequestBody GoLogAPI.dto.webhook.WebhookConfigRequest request) {
        CompanyResponse response = companyService.updateWebhook(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Testar Webhook da Empresa", description = "Envia um evento de teste TEST_PING para a URL configurada da empresa")
    @PostMapping("/{id}/webhook/test")
    public ResponseEntity<GoLogAPI.dto.webhook.WebhookTestResponse> testWebhook(@PathVariable("id") UUID id) {
        GoLogAPI.dto.webhook.WebhookTestResponse response = companyService.testWebhook(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Testar URL de Webhook", description = "Testa conectividade com qualquer URL de webhook")
    @PostMapping("/webhook/test-url")
    public ResponseEntity<GoLogAPI.dto.webhook.WebhookTestResponse> testWebhookUrl(
            @Valid @RequestBody GoLogAPI.dto.webhook.WebhookConfigRequest request) {
        GoLogAPI.dto.webhook.WebhookTestResponse response = companyService.testWebhookUrl(request.webhookUrl(), request.webhookSecret());
        return ResponseEntity.ok(response);
    }
}
