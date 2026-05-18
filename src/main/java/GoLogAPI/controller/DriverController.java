package GoLogAPI.controller;

import GoLogAPI.dto.driver.DriverCreateRequest;
import GoLogAPI.dto.driver.DriverUpdateRequest;
import GoLogAPI.dto.driver.DriverResponseList;
import GoLogAPI.dto.driver.DriverResponse;
import GoLogAPI.service.DriverService;
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
@RequestMapping("/driver")
@Tag(name = "Driver")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService){
        this.driverService = driverService;
    }

    @Operation(summary = "Create", description = "Create Driver")
    @PostMapping
    public ResponseEntity<DriverResponse> save(@Valid @RequestBody DriverCreateRequest driverCreateRequest) {
        DriverResponse driverResponse = driverService.save(driverCreateRequest);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("{id}")
                .buildAndExpand(driverResponse.id())
                .toUri();
        return ResponseEntity.created(uri).body(driverResponse);
    }

    @Operation(summary = "Display", description = "Display Driver")
    @RequestMapping(value = "{id}", method = {RequestMethod.GET, RequestMethod.HEAD})
    public ResponseEntity<DriverResponse> get(@PathVariable("id") UUID id){
        DriverResponse driverResponse = driverService.get(id);
        return ResponseEntity.ok(driverResponse);
    }

    @Operation(summary = "Display List", description = "Display Driver List")
    @GetMapping
    public ResponseEntity<List<DriverResponseList>> getAll(){
        List<DriverResponseList> driverResponses = driverService.getAll();
        return ResponseEntity.ok(driverResponses);
    }

    @Operation(summary = "Delete", description = "Delete Driver")
    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id){
        driverService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update", description = "Update Driver")
    @PutMapping("{id}")
    public ResponseEntity<DriverResponse> update(@PathVariable("id") UUID id, @Valid @RequestBody DriverCreateRequest driverCreateRequest){
        DriverResponse driverResponse = driverService.update(id, driverCreateRequest);
        return ResponseEntity.ok().body(driverResponse);
    }

    @Operation(summary = "Update Partial", description = "Update Partial Driver")
    @PatchMapping("{id}")
    public ResponseEntity<DriverResponse> updatePartial(@PathVariable("id") UUID id, @Valid @RequestBody DriverUpdateRequest driverUpdateRequest){
        DriverResponse driverResponse = driverService.updatePartial(id, driverUpdateRequest);
        return ResponseEntity.ok().body(driverResponse);
    }
}
