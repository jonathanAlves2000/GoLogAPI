package GoLogAPI.controller;

import GoLogAPI.dto.company.*;
import GoLogAPI.dto.delivery.DeliveryResponseList;
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
@Tag(name = "Company")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService){
        this.companyService = companyService;
    }

    @Operation(summary = "Create", description = "Create New Company")
    @PostMapping
    public ResponseEntity<CompanyCreateResponse> save(@Valid @RequestBody CompanyCreateRequest companyCreateRequest){
        CompanyCreateResponse companyCreateResponse = companyService.save(companyCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(companyCreateResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(companyCreateResponse);
    }

    @Operation(summary = "Display", description = "Display Company")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<CompanyResponse> get(@PathVariable("id") UUID id){
        CompanyResponse companyResponse = companyService.get(id);
        return ResponseEntity.ok(companyResponse);
    }

    @Operation(summary = "Display List", description = "Display Company List")
    @GetMapping
    public ResponseEntity<List<CompanyResponseList>> getAll(){
        List<CompanyResponseList> companyResponses = companyService.getAll();
        return ResponseEntity.ok().body(companyResponses);
    }

    @Operation(summary = "Delete", description = "Delete Company")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update", description = "Update Company")
    @PutMapping("{id}")
    public ResponseEntity<CompanyCreateResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody CompanyCreateRequest companyCreateRequest){
        CompanyCreateResponse companyCreateResponse = companyService.update(id, companyCreateRequest);
        return ResponseEntity.ok().body(companyCreateResponse);
    }

    @Operation(summary = "Update Partial", description = "Update Company Partial")
    @PatchMapping("{id}")
    public ResponseEntity<CompanyCreateResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody CompanyUpdateRequest companyUpdateRequest){
        CompanyCreateResponse companyCreateResponse = companyService.updatePartial(id, companyUpdateRequest);
        return ResponseEntity.ok().body(companyCreateResponse);
    }
}
