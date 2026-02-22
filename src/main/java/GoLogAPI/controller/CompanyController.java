package GoLogAPI.controller;

import GoLogAPI.dto.company.CompanyCreateRequest;
import GoLogAPI.dto.company.CompanyPatchRequest;
import GoLogAPI.dto.company.CompanyResponse;
import GoLogAPI.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/company")
public class CompanyController {

    private CompanyService companyService;

    public CompanyController(CompanyService companyService){
        this.companyService = companyService;
    }

    @PostMapping
    public CompanyResponse save(@Valid @RequestBody CompanyCreateRequest companyCreateRequest){
        return companyService.save(companyCreateRequest);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<CompanyResponse> get(@PathVariable UUID id){
        CompanyResponse companyResponse = companyService.get(id);
        return ResponseEntity.ok(companyResponse);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable UUID id){
        this.companyService.delete(id);
    }

    @PutMapping("{id}")
    public CompanyResponse update(@PathVariable UUID id, @Valid @RequestBody CompanyCreateRequest companyCreateRequest){
        return companyService.update(id, companyCreateRequest);
    }

    @PatchMapping("{id}")
    public CompanyResponse updatePartial(@PathVariable UUID id, @Valid @RequestBody CompanyPatchRequest companyPatchRequest){
        return companyService.updatePartial(id, companyPatchRequest);
    }
}
