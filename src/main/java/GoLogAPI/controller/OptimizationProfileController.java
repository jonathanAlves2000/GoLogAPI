package GoLogAPI.controller;

import GoLogAPI.dto.optimizationProfile.OptimizationProfileCreateRequest;
import GoLogAPI.dto.optimizationProfile.OptimizationProfileResponse;
import GoLogAPI.dto.optimizationProfile.OptimizationProfileUpdateRequest;
import GoLogAPI.service.OptimizationProfileService;
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
@RequestMapping("/optimization-profile")
@Tag(name = "Perfis de Otimização", description = "Gerenciamento de regras de negócio, pesos e custos de cálculo")
public class OptimizationProfileController {

    private final OptimizationProfileService optimizationProfileService;

    public OptimizationProfileController(OptimizationProfileService optimizationProfileService) {
        this.optimizationProfileService = optimizationProfileService;
    }

    @Operation(summary = "Cadastrar Perfil de Otimização", description = "Cadastra um novo perfil de regras e custos para a empresa")
    @PostMapping
    public ResponseEntity<OptimizationProfileResponse> save(@Valid @RequestBody OptimizationProfileCreateRequest request) {
        OptimizationProfileResponse response = optimizationProfileService.save(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @Operation(summary = "Exibir Perfil de Otimização", description = "Exibe os detalhes de um perfil pelo ID")
    @GetMapping("/{id}")
    public ResponseEntity<OptimizationProfileResponse> get(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(optimizationProfileService.get(id));
    }

    @Operation(summary = "Listar Perfis da Empresa", description = "Retorna todos os perfis de cálculo cadastrados para a empresa")
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<OptimizationProfileResponse>> getByCompany(@PathVariable("companyId") UUID companyId) {
        return ResponseEntity.ok(optimizationProfileService.getByCompany(companyId));
    }

    @Operation(summary = "Obter Perfil Padrão da Empresa", description = "Retorna o perfil padrão ativo configurado para a empresa")
    @GetMapping("/company/{companyId}/default")
    public ResponseEntity<OptimizationProfileResponse> getDefaultByCompany(@PathVariable("companyId") UUID companyId) {
        return ResponseEntity.ok(optimizationProfileService.getDefaultByCompany(companyId));
    }

    @Operation(summary = "Excluir Perfil de Otimização", description = "Exclui um perfil de otimização pelo ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        optimizationProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar Perfil de Otimização", description = "Atualiza regras e custos de um perfil existente")
    @PutMapping("/{id}")
    public ResponseEntity<OptimizationProfileResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody OptimizationProfileUpdateRequest request) {
        return ResponseEntity.ok(optimizationProfileService.update(id, request));
    }
}
