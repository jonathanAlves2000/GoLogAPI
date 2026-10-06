package GoLogAPI.controller;

import GoLogAPI.dto.tenant.TenantCreateRequest;
import GoLogAPI.dto.tenant.TenantResponse;
import GoLogAPI.dto.tenant.TenantSummaryResponse;
import GoLogAPI.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tenants")
@Tag(name = "Gerenciamento de Tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Operation(summary = "Criar Tenant", description = "Cria um novo Tenant (empresa matriz ou filial) com endereço e seu primeiro usuário Administrador. Apenas GoLog Master pode executar.")
    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@RequestBody @Valid TenantCreateRequest request) {
        TenantResponse response = tenantService.createTenant(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Listar Tenants", description = "Retorna os Tenants cadastrados. Usuário Master visualiza todos os tenants; outros visualizam seu grupo e filiais.")
    @GetMapping
    public ResponseEntity<List<TenantResponse>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @Operation(summary = "Obter Tenant por ID")
    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenantById(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.getTenantById(id));
    }

    @Operation(summary = "Listar Filiais de um Tenant Matriz")
    @GetMapping("/{id}/branches")
    public ResponseEntity<List<TenantResponse>> getBranches(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.getBranches(id));
    }

    @Operation(summary = "Métricas do Master de Tenants", description = "Retorna totais globais de tenants, filiais, usuários, veículos e remessas.")
    @GetMapping("/summary")
    public ResponseEntity<TenantSummaryResponse> getSummary() {
        return ResponseEntity.ok(tenantService.getSummary());
    }
}
