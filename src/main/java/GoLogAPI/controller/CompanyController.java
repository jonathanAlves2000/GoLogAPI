package GoLogAPI.controller;

import GoLogAPI.dto.company.*;
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

    private CompanyService companyService;

    public CompanyController(CompanyService companyService){
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> save(@Valid @RequestBody CompanyCreateRequest companyCreateRequest){
      CompanyResponse companyResponse = companyService.save(companyCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand()
                .toUri();
        return ResponseEntity.created(uri).body(companyResponse);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<CompanyResponse> get(@PathVariable("id") UUID id){
        CompanyResponse companyResponse = companyService.get(id);
        return ResponseEntity.ok(companyResponse);
    }

    @GetMapping
    public ResponseEntity<List<CompanyResponseList>> listAll(){
        List<CompanyResponseList> companyResponses = companyService.listAll();
        return ResponseEntity.ok().body(companyResponses);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable UUID id){
        this.companyService.delete(id);
    }

    @PutMapping("{id}")
    public CompanyResponse update(@PathVariable("id") UUID id, @Valid @RequestBody CompanyCreateRequest companyCreateRequest){
        return companyService.update(id, companyCreateRequest);
    }

    @PatchMapping("{id}")
    public ResponseEntity<CompanyResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody CompanyUpdateRequest companyUpdateRequest){
        CompanyResponse companyResponse = companyService.updatePartial(id, companyUpdateRequest);
        return ResponseEntity.ok().body(companyResponse);
    }
}
