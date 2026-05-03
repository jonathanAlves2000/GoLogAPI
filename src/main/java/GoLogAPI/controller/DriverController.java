package GoLogAPI.controller;

import GoLogAPI.dto.driver.DriverCreateRequest;
import GoLogAPI.dto.driver.DriverUpdateRequest;
import GoLogAPI.dto.driver.DriverResponseList;
import GoLogAPI.dto.driver.DriverResponse;
import GoLogAPI.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/driver")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService){
        this.driverService = driverService;
    }

    @PostMapping
    public ResponseEntity<DriverResponse> save(@Valid @RequestBody DriverCreateRequest driverCreateRequest) {
        DriverResponse driverResponse = driverService.save(driverCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(driverResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(driverResponse);
    }

    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<DriverResponse> get(@PathVariable("id") UUID id){
        DriverResponse driverResponse = driverService.get(id);
        return ResponseEntity.ok(driverResponse);
    }

    @GetMapping
    public ResponseEntity<List<DriverResponseList>> listAll(){
        List<DriverResponseList> driverResponses = driverService.listAll();
        return ResponseEntity.ok(driverResponses);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        driverService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("{id}")
    public ResponseEntity<DriverResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody DriverCreateRequest driverCreateRequest){
        DriverResponse driverResponse = driverService.update(id, driverCreateRequest);
        return ResponseEntity.ok().body(driverResponse);
    }

    @PatchMapping("{id}")
    public ResponseEntity<DriverResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody DriverUpdateRequest driverUpdateRequest){
        DriverResponse driverResponse = driverService.updatePartial(id, driverUpdateRequest);
        return ResponseEntity.ok().body(driverResponse);
    }
}
