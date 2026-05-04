package GoLogAPI.controller;

import GoLogAPI.dto.company.*;
import GoLogAPI.dto.delivery.DeliveryResponseList;
import GoLogAPI.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/company")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService){
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<CompanyCreateResponse> save(@Valid @RequestBody CompanyCreateRequest companyCreateRequest){
        CompanyCreateResponse companyCreateResponse = companyService.save(companyCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(companyCreateResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(companyCreateResponse);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<CompanyResponse> get(@PathVariable("id") UUID id){
        CompanyResponse companyResponse = companyService.get(id);
        return ResponseEntity.ok(companyResponse);
    }

    @GetMapping
    public ResponseEntity<List<CompanyResponseList>> getAll(){
        List<CompanyResponseList> companyResponses = companyService.getAll();
        return ResponseEntity.ok().body(companyResponses);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id){
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<CompanyCreateResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody CompanyCreateRequest companyCreateRequest){
        CompanyCreateResponse companyCreateResponse = companyService.update(id, companyCreateRequest);
        return ResponseEntity.ok().body(companyCreateResponse);
    }

    @PatchMapping("{id}")
    public ResponseEntity<CompanyCreateResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody CompanyUpdateRequest companyUpdateRequest){
        CompanyCreateResponse companyCreateResponse = companyService.updatePartial(id, companyUpdateRequest);
        return ResponseEntity.ok().body(companyCreateResponse);
    }
}
