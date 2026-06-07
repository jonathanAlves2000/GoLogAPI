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
@Tag(name = "Empresa")
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
}
