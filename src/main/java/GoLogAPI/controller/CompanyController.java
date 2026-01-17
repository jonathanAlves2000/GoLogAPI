package GoLogAPI.controller;

import GoLogAPI.model.Company;
import GoLogAPI.service.CompanyService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("company")
public class CompanyController {

    private CompanyService companyService;

    public CompanyController(CompanyService companyService){
        this.companyService = companyService;
    }

    @PostMapping
    public Company SaveCompany(@RequestBody Company company){
        return companyService.saveCompany(company);
    }

    @GetMapping("{id}")
    public Company GetCompanyById(@PathVariable int id){
        return companyService.getCompanyById(id);
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
