package GoLogAPI.controller;

import GoLogAPI.dto.CompanyDto;
import GoLogAPI.model.Company;
import GoLogAPI.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("company")
public class CompanyController {

    private CompanyService companyService;

    public CompanyController(CompanyService companyService){
        this.companyService = companyService;
    }

    @PostMapping
    public CompanyDto SaveCompany(@RequestBody @Valid CompanyDto companyDto){
        return companyService.saveCompany(companyDto);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<CompanyDto> GetCompanyById(@PathVariable int id){
        CompanyDto companyDto = companyService.getCompanyById(id);
        return ResponseEntity.ok(companyDto);
    }

    @DeleteMapping("{id}")
    public void DeleteCompanyById(@PathVariable int id){
        this.companyService.deleteCompanyById(id);
    }

    @PutMapping("{id}")
    public Company UpdateCompanyById(@PathVariable int id, @RequestBody Company company){
        return companyService.updateCompanyById(id, company);
    }
}
