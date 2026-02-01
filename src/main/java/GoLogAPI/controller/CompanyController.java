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
@RequestMapping("company")
public class CompanyController {

    private CompanyService companyService;

    public CompanyController(CompanyService companyService){
        this.companyService = companyService;
    }

    @PostMapping
    public CompanyResponse SaveCompany(@Valid @RequestBody CompanyCreateRequest companyCreateRequest){
        return companyService.saveCompany(companyCreateRequest);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<CompanyResponse> GetCompany(@PathVariable UUID id){
        CompanyResponse companyResponse = companyService.getCompany(id);
        return ResponseEntity.ok(companyResponse);
    }

    @DeleteMapping("{id}")
    public void DeleteCompany(@PathVariable UUID id){
        this.companyService.deleteCompany(id);
    }

    @PutMapping("{id}")
    public CompanyResponse PutCompany(@PathVariable UUID id, @Valid @RequestBody CompanyCreateRequest companyCreateRequest){
        return companyService.putCompany(id, companyCreateRequest);
    }

    @PatchMapping("{id}")
    public CompanyResponse PatchCompany(@PathVariable UUID id, @Valid @RequestBody CompanyPatchRequest companyPatchRequest){
        return companyService.patchCompany(id, companyPatchRequest);
    }
}
